package com.depromeet.domain.missionRecord.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash("MissionRecordTtl")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MissionRecordTtl {
    @Id private Long key;

    @TimeToLive private Long timeToLive;

    private LocalDateTime ttlFinishedAt;

    @Builder(access = AccessLevel.PRIVATE)
    public MissionRecordTtl(Long key, Long timeToLive, LocalDateTime ttlFinishedAt) {
        this.key = key;
        this.timeToLive = timeToLive;
        this.ttlFinishedAt = ttlFinishedAt;
    }

    public static MissionRecordTtl createMissionRecordTtl(
            Long key, Long timeToLive, LocalDateTime ttlFinishedAt) {
        return MissionRecordTtl.builder()
                .key(key)
                .timeToLive(timeToLive)
                .ttlFinishedAt(ttlFinishedAt)
                .build();
    }
}
