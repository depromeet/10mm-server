#!/bin/sh
# critical Redis 컨테이너의 /data(익명 볼륨 또는 컨테이너 레이어)를 named volume(redis-critical-data)으로 옮긴다.
# docker compose up -d 직전에 실행한다. named volume이 이미 있으면 아무것도 하지 않는다(멱등).
#
# 1. 실행 중인 Redis에서 AOF를 온라인으로 켜고 rewrite 완료를 기다린다.
#    (AOF 없이 --appendonly yes로 재시작하면 기존 dump.rdb를 무시하고 빈 상태로 뜬다)
# 2. graceful stop으로 디스크에 flush한 뒤 /data를 named volume으로 복사한다.
# 3. 실패하면 기존 컨테이너를 다시 띄우고 0이 아닌 코드로 종료해 배포를 중단시킨다.
set -eu

CONTAINER="${REDIS_CONTAINER:-redis}"
VOLUME="${REDIS_VOLUME:-redis-critical-data}"
IMAGE="${REDIS_IMAGE:-redis:alpine}"

log() { echo "[migrate-redis-volume] $*"; }

if docker volume inspect "$VOLUME" >/dev/null 2>&1; then
    log "volume '$VOLUME' already exists, skip"
    exit 0
fi
if ! docker inspect "$CONTAINER" >/dev/null 2>&1; then
    log "container '$CONTAINER' not found, creating empty volume '$VOLUME'"
    docker volume create "$VOLUME" >/dev/null
    exit 0
fi

stopped=false
restore() {
    status=$?
    if [ "$status" -ne 0 ]; then
        log "migration failed (exit $status)"
        docker volume rm "$VOLUME" >/dev/null 2>&1 || true
        if [ "$stopped" = true ]; then
            log "restarting original container '$CONTAINER'"
            docker start "$CONTAINER" >/dev/null || true
        fi
    fi
}
trap restore EXIT

redis_info() {
    docker exec "$CONTAINER" redis-cli INFO persistence | tr -d '\r' | grep "^$1:" | cut -d: -f2
}

# 멈춰 있는 컨테이너도 RDB만 있을 수 있으므로 띄워서 AOF로 전환한다.
if [ "$(docker inspect -f '{{.State.Running}}' "$CONTAINER")" != true ]; then
    stopped=true
    docker start "$CONTAINER" >/dev/null
    i=0
    until docker exec "$CONTAINER" redis-cli PING 2>/dev/null | grep -q PONG; do
        i=$((i + 1))
        [ "$i" -le 30 ] || { log "redis did not start in 30s"; exit 1; }
        sleep 1
    done
fi
stopped=false

log "keys before migration: $(docker exec "$CONTAINER" redis-cli DBSIZE | tr -d '\r')"
if [ "$(redis_info aof_enabled)" != 1 ]; then
    docker exec "$CONTAINER" redis-cli CONFIG SET appendonly yes >/dev/null
fi
i=0
while [ "$(redis_info aof_rewrite_in_progress)" != 0 ] \
    || [ "$(redis_info aof_rewrite_scheduled)" != 0 ]; do
    i=$((i + 1))
    [ "$i" -le 120 ] || { log "AOF rewrite did not finish in 120s"; exit 1; }
    sleep 1
done
[ "$(redis_info aof_last_bgrewrite_status)" = ok ] || { log "AOF rewrite failed"; exit 1; }
log "AOF enabled"

stopped=true
docker stop -t 30 "$CONTAINER" >/dev/null

docker volume create "$VOLUME" >/dev/null
# 이미지 버전에 따라 /data가 익명 볼륨(redis:7)이거나 컨테이너 레이어(redis:8)이므로
# 두 경우 모두 읽히는 docker cp로 꺼낸다.
docker cp "$CONTAINER":/data - \
    | docker run --rm -i -v "$VOLUME":/target "$IMAGE" \
        sh -c 'mkdir /tmp/x && tar -x -C /tmp/x && cp -a /tmp/x/data/. /target/'
docker run --rm -v "$VOLUME":/target "$IMAGE" test -d /target/appendonlydir \
    || { log "copied volume has no appendonlydir"; exit 1; }

log "copied /data of '$CONTAINER' into volume '$VOLUME'"
log "old container data (anonymous volume, if any) is kept; do not run 'docker volume prune' until the new Redis is verified"
