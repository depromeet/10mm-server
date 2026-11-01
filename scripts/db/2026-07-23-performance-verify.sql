SELECT member_id, mission_record_id, count(*) AS duplicate_count
FROM reaction
GROUP BY member_id, mission_record_id
HAVING count(*) > 1;

SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = current_schema()
  AND indexname IN (
      'idx_member_relation_target_source',
      'idx_member_relation_source_created',
      'idx_member_relation_target_created',
      'idx_mission_record_complete_feed',
      'idx_mission_member_visibility_status',
      'idx_mission_status_remind_at',
      'idx_mission_record_mission_status_started',
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

EXPLAIN (ANALYZE, BUFFERS)
SELECT source_id, target_id
FROM member_relation
WHERE target_id = 1
ORDER BY source_id
LIMIT 21;
