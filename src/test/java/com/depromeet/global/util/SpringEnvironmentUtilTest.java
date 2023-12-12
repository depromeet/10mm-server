package com.depromeet.global.util;

import static com.depromeet.global.util.SpringEnvironmentUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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

    @Test
    void 상용_환경이라면_isProdProfile은_true를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {PROD});

        // when
        // then
        assertTrue(springEnvironmentUtil.isProdProfile());
    }

    @Test
    void 상용_환경이_아니라면_isProdProfile은_false를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {DEV});

        // when
        // then
        assertFalse(springEnvironmentUtil.isProdProfile());
    }

    @Test
    void 테스트_환경이라면_isDevProfile은_true를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {DEV});

        // when
        // then
        assertTrue(springEnvironmentUtil.isDevProfile());
    }

    @Test
    void 테스트_환경이_아니라면_isDevProfile은_false를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {LOCAL});

        // when
        // then
        assertFalse(springEnvironmentUtil.isDevProfile());
    }

    @Test
    void 로컬_환경이라면_isProdAndDevProfile은_false를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {LOCAL});

        // when
        // then
        assertFalse(springEnvironmentUtil.isProdAndDevProfile());
    }

    @Test
    void 로컬_환경이_아니라면_isProdAndDevProfile은_true를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {DEV});

        // when
        // then
        assertTrue(springEnvironmentUtil.isProdAndDevProfile());
    }

    @Test
    void 상용_환경이라면_getCurrentProfile는은_prod를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {PROD});

        // when
        // then
        assertEquals(springEnvironmentUtil.getCurrentProfile(), PROD);
    }

    @Test
    void 테스트_환경이라면_getCurrentProfile는은_dev를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {DEV});

        // when
        // then
        assertEquals(springEnvironmentUtil.getCurrentProfile(), DEV);
    }

    @Test
    void 로컬_환경이라면_getCurrentProfile는은_local을_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {LOCAL});

        // when
        // then
        assertEquals(springEnvironmentUtil.getCurrentProfile(), LOCAL);
    }
}
