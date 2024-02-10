package com.depromeet.domain.reaction.dto.response;

import com.depromeet.domain.member.dto.MemberProfileDto;
import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ReactionGroupByEmojiResponse(
        EmojiType emojiType, Integer count, List<ReactionDetailDto> members) {

    public static ReactionGroupByEmojiResponse of(Map.Entry<EmojiType, List<Reaction>> entry) {
        return new ReactionGroupByEmojiResponse(
                entry.getKey(),
                entry.getValue().size(),
                entry.getValue().stream().map(ReactionDetailDto::of).toList());
    }

    record ReactionDetailDto(
            Long reactionId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            MemberProfileDto memberProfileDto) {
        public static ReactionDetailDto of(Reaction reaction) {
            return new ReactionDetailDto(
                    reaction.getId(),
                    reaction.getCreatedAt(),
                    reaction.getUpdatedAt(),
                    MemberProfileDto.from(reaction.getMember()));
        }
    }
}
