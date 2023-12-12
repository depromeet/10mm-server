package com.depromeet.global.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class SpringEnvironmentUtilTest {
    @Mock private Environment environment;

    @InjectMocks private SpringEnvironmentUtil springEnvironmentUtil;

    private final String PROD = "prod";
    private final String DEV = "dev";
    private final String LOCAL = "local";

    @Test
    @DisplayName("상용 환경이라면 isProdProfile은 true를 반환한다")
    void 상용_환경이라면_isProdProfile은_true를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {PROD});

        // when
        // then
        assertTrue(springEnvironmentUtil.isProdProfile());
    }

    @Test
    @DisplayName("상용 환경이 아니라면 isProdProfile은 false를 반환한다")
    void 상용_환경이_아니라면_isProdProfile은_false를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {DEV});

        // when
        // then
        assertFalse(springEnvironmentUtil.isProdProfile());
    }

    @Test
    @DisplayName("테스트 환경이라면 isDevProfile은 true를 반환한다")
    void 테스트_환경이라면_isDevProfile은_true를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {DEV});

        // when
        // then
        assertTrue(springEnvironmentUtil.isDevProfile());
    }

    @Test
    @DisplayName("테스트 환경이 아니라면 isDevProfile은 false를 반환한다")
    void 테스트_환경이_아니라면_isDevProfile은_false를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {LOCAL});

        // when
        // then
        assertFalse(springEnvironmentUtil.isDevProfile());
    }

    @Test
    @DisplayName("로컬 환경이라면 isProdAndDevProfile은 false를 반환한다")
    void 로컬_환경이라면_isProdAndDevProfile은_false를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {LOCAL});

        // when
        // then
        assertFalse(springEnvironmentUtil.isProdAndDevProfile());
    }

    @Test
    @DisplayName("로컬 환경이_아니라면 isProdAndDevProfile은 true를 반환한다")
    void 로컬_환경이_아니라면_isProdAndDevProfile은_true를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {DEV});

        // when
        // then
        assertTrue(springEnvironmentUtil.isProdAndDevProfile());
    }

    @Test
    @DisplayName("상용 환경이라면 getCurrentProfile는 prod를 반환한다")
    void 상용_환경이라면_getCurrentProfile는은_prod를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {PROD});

        // when
        // then
        assertEquals(springEnvironmentUtil.getCurrentProfile(), PROD);
    }

    @Test
    @DisplayName("테스트 환경이라면 getCurrentProfile는 dev를 반환한다")
    void 테스트_환경이라면_getCurrentProfile는은_dev를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {DEV});

        // when
        // then
        assertEquals(springEnvironmentUtil.getCurrentProfile(), DEV);
    }

    @Test
    @DisplayName("로컬 환경이라면 getCurrentProfile는 local을 반환한다")
    void 로컬_환경이라면_getCurrentProfile는은_local을_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {LOCAL});

        // when
        // then
        assertEquals(springEnvironmentUtil.getCurrentProfile(), LOCAL);
    }
}
