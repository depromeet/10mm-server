package com.depromeet.domain.reaction.dto.response;

import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import io.swagger.v3.oas.annotations.media.Schema;

public record ReactionUpdateResponse(
        @Schema(description = "이모지 타입", defaultValue = "PURPLE_HEART") EmojiType emojiType) {
    public static ReactionUpdateResponse from(Reaction reaction) {
        return new ReactionUpdateResponse(reaction.getEmojiType());
    }
}
