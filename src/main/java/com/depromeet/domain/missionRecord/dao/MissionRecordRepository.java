package com.depromeet.domain.missionRecord.dao;

import com.depromeet.domain.missionRecord.domain.MissionRecord;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MissionRecordRepository
        extends JpaRepository<MissionRecord, Long>, MissionRecordRepositoryCustom {

    @Modifying
    @Query(
            """
            delete from MissionRecord record
            where record.id = :recordId
              and record.uploadStatus <> com.depromeet.domain.missionRecord.domain.ImageUploadStatus.COMPLETE
              and record.uploadDeadlineAt <= :now
            """)
    int deleteExpiredIncompleteRecord(
            @Param("recordId") Long recordId, @Param("now") LocalDateTime now);

    @Modifying
    @Query(
            """
            delete from MissionRecord record
            where record.uploadStatus <> com.depromeet.domain.missionRecord.domain.ImageUploadStatus.COMPLETE
              and record.uploadDeadlineAt <= :now
            """)
    int deleteExpiredIncompleteRecords(@Param("now") LocalDateTime now);
}
