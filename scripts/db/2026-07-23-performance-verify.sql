SELECT member_id, mission_record_id, count(*) AS duplicate_count
FROM reaction
GROUP BY member_id, mission_record_id
HAVING count(*) > 1;

-- 모든 행의 is_valid가 true여야 한다. CREATE INDEX CONCURRENTLY가 실패하면 invalid 인덱스가 남고,
-- IF NOT EXISTS 때문에 재실행해도 다시 만들어지지 않는다. 그 경우 DROP INDEX CONCURRENTLY 후 재생성한다.
SELECT index_class.relname AS indexname, pg_index.indisvalid AS is_valid,
       pg_get_indexdef(pg_index.indexrelid) AS indexdef
FROM pg_index
JOIN pg_class index_class ON index_class.oid = pg_index.indexrelid
JOIN pg_namespace ON pg_namespace.oid = index_class.relnamespace
WHERE pg_namespace.nspname = current_schema()
  AND index_class.relname IN (
      'idx_member_relation_target_source',
      'idx_member_relation_source_created',
      'idx_member_relation_target_created',
      'idx_mission_record_complete_feed',
      'idx_mission_member_visibility_status',
      'idx_mission_status_remind_at',
      'idx_mission_record_mission_status_started',
      'idx_mission_record_incomplete_deadline',
      'idx_reaction_record_created',
      'uk_reaction_member_record',
      'idx_comment_record_created',
      'idx_notification_target_created',
      'idx_member_nickname_trgm'
  )
ORDER BY indexname;

SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = current_schema()
  AND table_name = 'mission_record'
  AND column_name = 'upload_deadline_at';

-- 0이어야 한다. 남아 있으면 GET /missions가 MISSION_STATUS_MISMATCH로 실패한다.
SELECT count(*) AS incomplete_records_without_deadline
FROM mission_record
WHERE upload_status IN ('NONE', 'PENDING')
  AND upload_deadline_at IS NULL;

EXPLAIN (ANALYZE, BUFFERS)
SELECT mr.mission_record_id
FROM mission_record mr
JOIN mission m ON m.mission_id = mr.mission_id
WHERE mr.upload_status = 'COMPLETE'
  AND m.visibility = 'ALL'
ORDER BY mr.mission_record_id DESC
LIMIT 21;

-- 팔로워 cursor 페이지 (FollowService.findFollowerPage, 첫 페이지 + 두 번째 페이지)
EXPLAIN (ANALYZE, BUFFERS)
SELECT mr.member_relation_id, mr.created_at, m.*
FROM member_relation mr
JOIN member m ON m.member_id = mr.source_id
WHERE mr.target_id = 1
ORDER BY mr.created_at DESC, mr.member_relation_id DESC
LIMIT 21;

EXPLAIN (ANALYZE, BUFFERS)
WITH cursor_row AS (
    SELECT created_at, member_relation_id FROM member_relation
    WHERE target_id = 1 ORDER BY created_at DESC, member_relation_id DESC OFFSET 20 LIMIT 1
)
SELECT mr.member_relation_id, mr.created_at, m.*
FROM member_relation mr
JOIN member m ON m.member_id = mr.source_id, cursor_row c
WHERE mr.target_id = 1
  AND (mr.created_at < c.created_at
       OR (mr.created_at = c.created_at AND mr.member_relation_id < c.member_relation_id))
ORDER BY mr.created_at DESC, mr.member_relation_id DESC
LIMIT 21;

-- 팔로잉 cursor 페이지 (FollowService.findFollowingPage, 첫 페이지)
EXPLAIN (ANALYZE, BUFFERS)
SELECT mr.member_relation_id, mr.created_at, m.*
FROM member_relation mr
JOIN member m ON m.member_id = mr.target_id
WHERE mr.source_id = 1
ORDER BY mr.created_at DESC, mr.member_relation_id DESC
LIMIT 21;

-- 만료 기록 정리 (MissionRecordExpirationScheduler)
EXPLAIN (ANALYZE, BUFFERS)
SELECT mission_record_id
FROM mission_record
WHERE upload_status <> 'COMPLETE'
  AND upload_deadline_at <= now();
