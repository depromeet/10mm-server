# API performance rollout runbook

## Order

1. Confirm no duplicate `(member_id, mission_record_id)` rows in `reaction` with
   `scripts/db/2026-07-23-performance-verify.sql`.
2. Apply `scripts/db/2026-07-23-performance-indexes.sql` one statement at a time. Do not wrap
   `CREATE INDEX CONCURRENTLY` statements in a transaction.
3. Run the verification SQL and confirm the hot queries use the new indexes without a large
   sequential scan or disk sort.
4. Start `redis` on port 6379 for refresh tokens and deadline markers, and `redis-cache` on port
   6380 for evictable query cache entries.
5. Deploy the application, then verify `/10mm-actuator/prometheus` exposes
   `cache_requests_total`, `cache_fallbacks_total`, and `cache_eviction_errors_total`.
   Also verify `mission_record_expiration_recovered_total` increases when an expired deadline
   marker removes an incomplete record.
6. Exercise feed, follow summary, follow mutation, member search, and notification cursor APIs.

## Redis policy

- Critical Redis uses `noeviction` and AOF. Alert before memory reaches 80%.
- Cache Redis uses `allkeys-lru`; every `follow:summary:v1:*` value also has a 60–75 second TTL.
- A cache outage must increase fallback/error metrics but must not fail follow reads or writes.

## Rollback

1. Roll back the application first.
2. Run `scripts/db/2026-07-23-performance-rollback.sql` one statement at a time.
3. Drop `upload_deadline_at` only after no deployed application reads or writes it.
4. The `pg_trgm` extension is intentionally retained because other indexes may depend on it.
