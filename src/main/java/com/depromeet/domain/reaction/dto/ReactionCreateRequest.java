package com.depromeet.domain.reaction.dto;

import com.depromeet.domain.reaction.domain.EmojiType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReactionCreateRequest(
        @NotNull(message = "미션 기록 아이디는 비워둘 수 없습니다.")
                @Schema(description = "미션 기록 아이디", defaultValue = "1")
                Long missionRecordId,
        @NotNull(message = "이모지 타입은 비워둘 수 없습니다.")
                @Schema(description = "이모지 타입", defaultValue = "PURPLE_HEART")
                EmojiType emojiType) {}
