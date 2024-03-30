package com.depromeet.domain.reaction.dto.response;

import static java.util.Comparator.*;

import com.depromeet.domain.member.dto.MemberProfileDto;
import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ReactionGroupByEmojiResponse(
        EmojiType emojiType, Integer count, List<ReactionDetailDto> reactions) {

    public static ReactionGroupByEmojiResponse from(Map.Entry<EmojiType, List<Reaction>> entry) {
        return new ReactionGroupByEmojiResponse(
                entry.getKey(),
                entry.getValue().size(),
                entry.getValue().stream().map(ReactionDetailDto::from).toList());
    }

    public record ReactionDetailDto(
            Long reactionId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            MemberProfileDto memberProfile) {
        public static ReactionDetailDto from(Reaction reaction) {
            return new ReactionDetailDto(
                    reaction.getId(),
                    reaction.getCreatedAt(),
                    reaction.getUpdatedAt(),
                    MemberProfileDto.from(reaction.getMember()));
        }
    }

    // TODO: 리액션 그룹 단위에 해당하는 DTO가 컬렉션 로직을 들고있는 것이 올바른지 고민 필요
    public static List<ReactionGroupByEmojiResponse> groupByEmojiType(List<Reaction> reactions) {
        return reactions.stream()
                .collect(Collectors.groupingBy(Reaction::getEmojiType))
                .entrySet()
                .stream()
                .map(ReactionGroupByEmojiResponse::from)
                .sorted(comparing(ReactionGroupByEmojiResponse::count).reversed())
                .toList();
    }
}
