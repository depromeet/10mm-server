package com.depromeet.domain.ranking.dto;

import com.depromeet.domain.member.domain.Member;

public record RankingDto(Member member, long symbolStack) {
    public static RankingDto of(Member member, long symbolStack) {
        return new RankingDto(member, symbolStack);
    }
}
