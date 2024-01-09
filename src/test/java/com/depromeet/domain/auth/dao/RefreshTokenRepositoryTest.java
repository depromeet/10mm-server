package com.depromeet.domain.auth.dao;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.TestRedisConfig;
import com.depromeet.domain.auth.domain.RefreshToken;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@DataRedisTest
class RefreshTokenRepositoryTest {

    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @AfterEach
    public void tearDown() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    void 리프레시_토큰을_저장한다() {
        // given
        RefreshToken refreshToken =
                RefreshToken.builder().memberId(1L).token("testRefreshToken").ttl(1000).build();

        // when
        refreshTokenRepository.save(refreshToken);

        // then
        Optional<RefreshToken> savedRefreshToken = refreshTokenRepository.findById(1L);
        assertTrue(savedRefreshToken.isPresent());
    }

    @Test
    void 리프레시_토큰을_삭제한다() {
        // given
        RefreshToken refreshToken =
                RefreshToken.builder().memberId(2L).token("testRefreshToken").ttl(1000).build();
        refreshTokenRepository.save(refreshToken);

        // when
        refreshTokenRepository.delete(refreshToken);

        // then
        assertFalse(refreshTokenRepository.findById(refreshToken.getMemberId()).isPresent());
    }

    @Test
    void 리프레시_토큰을_조회한다() {
        // given
        RefreshToken refreshToken =
                RefreshToken.builder().memberId(3L).token("testRefreshToken").ttl(1000).build();
        refreshTokenRepository.save(refreshToken);

        // when
        Optional<RefreshToken> savedRefreshToken = refreshTokenRepository.findById(3L);

        // then
        assertTrue(savedRefreshToken.isPresent());
        assertEquals(3L, savedRefreshToken.get().getMemberId());
        assertEquals("testRefreshToken", savedRefreshToken.get().getToken());
    }
}
