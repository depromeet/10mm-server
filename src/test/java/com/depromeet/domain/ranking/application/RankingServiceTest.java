package com.depromeet.domain.ranking.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.domain.ranking.dao.RankingRepository;
import com.depromeet.domain.ranking.domain.Ranking;
import com.depromeet.domain.ranking.dto.response.RankingResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RankingServiceTest {

    @Autowired private RankingService rankingService;
    @Autowired private RankingRepository rankingRepository;
    @Autowired private MemberRepository memberRepository;

    @Test
    void 마지막으로_저장된_랭킹을_조회한다() {
        Member firstMember =
                memberRepository.save(
                        Member.createNormalMember(
                                OauthInfo.createOauthInfo("first", "first", "first"), "first"));
        Member secondMember =
                memberRepository.save(
                        Member.createNormalMember(
                                OauthInfo.createOauthInfo("second", "second", "second"), "second"));
        rankingRepository.saveAll(
                List.of(
                        Ranking.createRanking(10L, firstMember),
                        Ranking.createRanking(20L, secondMember)));

        List<RankingResponse> rankings = rankingService.findAllRanking();

        assertThat(rankings)
                .extracting(RankingResponse::memberId)
                .containsExactly(secondMember.getId(), firstMember.getId());
        assertThat(rankings).extracting(RankingResponse::rank).containsExactly(1L, 2L);
    }
}
