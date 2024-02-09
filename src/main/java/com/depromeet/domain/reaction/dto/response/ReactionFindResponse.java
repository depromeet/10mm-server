package com.depromeet.domain.reaction.dto.response;

import static java.util.stream.Collectors.*;

import com.depromeet.domain.member.dto.MemberProfileDto;
import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record ReactionFindResponse(Map<EmojiType, ReactionDetail> emojiTypeCountMap) {

    public static ReactionFindResponse from(List<Reaction> reactions) {
        Map<EmojiType, ReactionDetail> emojiTypeCountMap =
                reactions.stream()
                        .collect(
                                groupingBy(
                                        Reaction::getEmojiType,
                                        () -> new EnumMap<>(EmojiType.class),
                                        collectingAndThen(toList(), ReactionDetail::from)));
        return new ReactionFindResponse(emojiTypeCountMap);
    }

    record ReactionDetail(Integer count, List<MemberProfileDto> members) {
        public static ReactionDetail from(List<Reaction> reactions) {
            List<MemberProfileDto> memberProfileDtos =
                    reactions.stream()
                            .map(Reaction::getMember)
                            .map(MemberProfileDto::from)
                            .toList();
            return new ReactionDetail(reactions.size(), memberProfileDtos);
        }
    }
}
