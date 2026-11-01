DROP INDEX CONCURRENTLY IF EXISTS idx_member_relation_target_source;
DROP INDEX CONCURRENTLY IF EXISTS idx_member_relation_source_created;
DROP INDEX CONCURRENTLY IF EXISTS idx_member_relation_target_created;
DROP INDEX CONCURRENTLY IF EXISTS idx_mission_record_complete_feed;
DROP INDEX CONCURRENTLY IF EXISTS idx_mission_member_visibility_status;
DROP INDEX CONCURRENTLY IF EXISTS idx_mission_status_remind_at;
DROP INDEX CONCURRENTLY IF EXISTS idx_mission_record_mission_status_started;
DROP INDEX CONCURRENTLY IF EXISTS idx_reaction_record_created;
DROP INDEX CONCURRENTLY IF EXISTS uk_reaction_member_record;
DROP INDEX CONCURRENTLY IF EXISTS idx_comment_record_created;
DROP INDEX CONCURRENTLY IF EXISTS idx_notification_target_created;
DROP INDEX CONCURRENTLY IF EXISTS idx_member_nickname_trgm;

-- Run only after rolling the application back to a version that does not use this column.
ALTER TABLE mission_record DROP COLUMN IF EXISTS upload_deadline_at;
