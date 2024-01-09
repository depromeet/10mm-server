package com.depromeet.domain.missionRecord.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("MissionRecordTTL")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MissionRecordTTL {
    @Id private String key;

    @TimeToLive private Long timeToLive;

    @Builder(access = AccessLevel.PRIVATE)
    private MissionRecordTTL(String key, Long timeToLive) {
        this.key = key;
        this.timeToLive = timeToLive;
    }

    public static MissionRecordTTL createMissionRecordTTL(String key, Long timeToLive) {
        return MissionRecordTTL.builder().key(key).timeToLive(timeToLive).build();
    }
}
