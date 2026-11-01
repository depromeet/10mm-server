package com.depromeet.global.common.response;

import java.util.List;

public record CursorSliceResponse<T>(List<T> content, String nextCursor, boolean hasNext) {
    public static <T> CursorSliceResponse<T> of(
            List<T> content, String nextCursor, boolean hasNext) {
        return new CursorSliceResponse<>(content, nextCursor, hasNext);
    }
}
