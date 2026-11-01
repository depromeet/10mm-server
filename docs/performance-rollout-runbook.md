# API performance rollout runbook

## Order

1. Confirm no duplicate `(member_id, mission_record_id)` rows in `reaction` with
   `scripts/db/2026-07-23-performance-verify.sql`.
2. Apply `scripts/db/2026-07-23-performance-indexes.sql` one statement at a time **before**
   deploying the application; it reads and writes `mission_record.upload_deadline_at`. Do not
   wrap `CREATE INDEX CONCURRENTLY` statements in a transaction. The script also backfills
   `upload_deadline_at` for incomplete (`NONE`, `PENDING`) records created before the column
   existed.
3. Run the verification SQL and confirm every listed index has `is_valid = true`, the hot
   queries use the new indexes without a large sequential scan or disk sort, and that
   `incomplete_records_without_deadline` is 0. If an index is invalid, drop it with
   `DROP INDEX CONCURRENTLY` and run its `CREATE INDEX CONCURRENTLY` statement again.
4. **Critical Redis moves to the named volume `redis-critical-data` with AOF on.** The deploy
   workflows run `scripts/deploy/migrate-redis-volume.sh` right before `docker compose up -d`, so
   no manual step is needed. The script is idempotent and only acts on the first deploy:
   - Enables AOF online (`CONFIG SET appendonly yes`) and waits for the rewrite. Restarting with
     `--appendonly yes` and no AOF file makes Redis ignore `dump.rdb` and start empty, which would
     log out every user.
   - Stops the old `redis` container gracefully (Redis is unavailable for a few seconds), then
     copies `/data` into the volume with `docker cp`. This works whether `/data` was an anonymous
     volume (`redis:7`) or the container layer (`redis:8`, which declares no volume).
   - On failure it removes the half-written volume, restarts the old container, and exits non-zero
     so the deploy stops. The volume is `external`, so `docker compose up -d` refuses to start
     Redis on an empty volume if the script did not run.

   After the deploy, confirm `docker exec redis redis-cli DBSIZE` matches the
   `keys before migration` line in the deploy log. Keep the old container's anonymous volume (if
   any) and avoid `docker volume prune` until the new Redis is verified.
5. Start `redis` on port 6379 for refresh tokens and deadline markers, and `redis-cache` on port
   6380 (bound to `127.0.0.1`) for evictable query cache entries.
6. Deploy the application, then verify `/10mm-actuator/prometheus` exposes
   `cache_requests_total`, `cache_fallbacks_total`, and `cache_eviction_errors_total`.
   Also verify `mission_record_expiration_recovered_total` increases when an expired deadline
   marker removes an incomplete record.
7. Exercise feed, follow summary, follow mutation, member search, and notification cursor APIs.

## Redis policy

- Critical Redis uses `noeviction` and AOF. Alert before memory reaches 80%.
- Cache Redis uses `allkeys-lru`; every `follow:summary:v1:*` value also has a 60–75 second TTL.
- A cache outage must increase fallback/error metrics but must not fail follow reads or writes.

## Rollback

1. Roll back the application first.
2. Run `scripts/db/2026-07-23-performance-rollback.sql` one statement at a time.
3. Drop `upload_deadline_at` only after no deployed application reads or writes it.
4. The `pg_trgm` extension is intentionally retained because other indexes may depend on it.
