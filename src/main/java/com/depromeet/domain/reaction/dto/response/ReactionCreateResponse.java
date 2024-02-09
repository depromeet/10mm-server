package com.depromeet.domain.reaction.dto.response;

import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import io.swagger.v3.oas.annotations.media.Schema;

public record ReactionCreateResponse(
        @Schema(description = "리액션 ID", defaultValue = "1") Long reactionId,
        @Schema(description = "이모지 타입", defaultValue = "PURPLE_HEART") EmojiType emojiType,
        @Schema(description = "멤버 ID", defaultValue = "1") Long memberId,
        @Schema(description = "미션기록 ID", defaultValue = "1") Long missionRecordId) {

    public static ReactionCreateResponse from(Reaction reaction) {
        return new ReactionCreateResponse(
                reaction.getId(),
                reaction.getEmojiType(),
                reaction.getMember().getId(),
                reaction.getMissionRecord().getId());
    }
}
