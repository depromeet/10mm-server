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
4. **Enable AOF on the running critical Redis before deploying.** The new `docker-compose.yaml`
   starts `redis` with `--appendonly yes`. If Redis restarts with AOF on but no AOF file exists,
   it starts empty and ignores the existing `dump.rdb`, so every refresh token is lost and all
   users are logged out. Convert the data online first:

   ```bash
   docker exec redis redis-cli CONFIG SET appendonly yes
   # Repeat until both values are 0, and aof_last_bgrewrite_status is ok
   docker exec redis redis-cli INFO persistence | grep -E 'aof_rewrite_in_progress|aof_rewrite_scheduled|aof_last_bgrewrite_status'
   docker exec redis redis-cli DBSIZE
   ```

   After the deploy recreates the container, confirm `DBSIZE` is unchanged.
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
