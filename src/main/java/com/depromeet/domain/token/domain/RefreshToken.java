package com.depromeet.domain.token.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash(value = "refreshToken")
public class RefreshToken {

    @Id private Long memberId;
    private String token;
    @TimeToLive private long ttl;

    @Builder
    public RefreshToken(Long memberId, String token, long ttl) {
        this.memberId = memberId;
        this.token = token;
        this.ttl = ttl;
    }
}
