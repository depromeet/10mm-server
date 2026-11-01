package com.depromeet.global.util;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.global.error.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class CursorCodecTest {

    private final CursorCodec cursorCodec =
            new CursorCodec(
                    new ObjectMapper()
                            .findAndRegisterModules()
                            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));

    @Test
    void cursor를_opaque_문자열로_왕복한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 23, 12, 30);

        // when
        List<String> values = cursorCodec.decode(cursorCodec.encode(createdAt, 10L), 2);

        // then
        assertEquals(createdAt, cursorCodec.dateTimeValue(values, 0));
        assertEquals(10L, cursorCodec.longValue(values, 1));
    }

    @Test
    void 변조된_cursor는_잘못된_요청으로_거절한다() {
        assertThrows(CustomException.class, () -> cursorCodec.decode("not-a-cursor", 2));
    }
}
