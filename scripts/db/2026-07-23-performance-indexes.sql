-- Run each CREATE INDEX CONCURRENTLY outside a transaction.
-- Verify duplicate reactions before creating the unique index.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE mission_record
    ADD COLUMN IF NOT EXISTS upload_deadline_at timestamp;

-- 컬럼 추가 전 생성된 인증 대기 기록에 마감 시각(생성 + 10분) 백필.
-- 마감이 지난 기록은 MissionRecordExpirationScheduler가 1분 주기로 삭제한다.
UPDATE mission_record
SET upload_deadline_at = created_at + interval '10 minutes'
WHERE upload_status = 'NONE'
  AND upload_deadline_at IS NULL;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_member_relation_target_source
    ON member_relation (target_id, source_id);

-- 팔로잉/팔로워 cursor 페이지 (created_at DESC, member_relation_id DESC 정렬)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_member_relation_source_created
    ON member_relation (source_id, created_at DESC, member_relation_id DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_member_relation_target_created
    ON member_relation (target_id, created_at DESC, member_relation_id DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mission_record_complete_feed
    ON mission_record (mission_record_id DESC, mission_id)
    WHERE upload_status = 'COMPLETE';

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mission_member_visibility_status
    ON mission (member_id, visibility, duration_status);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mission_status_remind_at
    ON mission (duration_status, remind_at, started_at, finished_at);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mission_record_mission_status_started
    ON mission_record (mission_id, upload_status, started_at);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_reaction_record_created
    ON reaction (mission_record_id, created_at DESC);

CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uk_reaction_member_record
    ON reaction (member_id, mission_record_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_comment_record_created
    ON comment (mission_record_id, created_at ASC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_target_created
    ON notification (target_id, created_at DESC, notification_id DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_member_nickname_trgm
    ON member USING gin (nickname gin_trgm_ops);
