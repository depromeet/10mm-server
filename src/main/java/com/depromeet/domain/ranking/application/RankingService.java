package com.depromeet.domain.ranking.application;

import com.depromeet.domain.mission.dto.response.MissionSymbolStackResponse;
import com.depromeet.domain.ranking.dao.RankingRepository;
import com.depromeet.domain.ranking.dto.response.RankingResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RankingService {

    private final RankingRepository rankingRepository;

    @Transactional(readOnly = true)
    public List<RankingResponse> findAllRanking() {
        return rankingRepository.findTop50ByOrderBySymbolStackDesc().stream()
                .map(RankingResponse::from)
                .toList();
    }

    public void updateSymbolStack(List<MissionSymbolStackResponse> allMissionSymbolStack) {
        for (MissionSymbolStackResponse missionSymbolStackResponse : allMissionSymbolStack) {
            rankingRepository.updateSymbolStackAndMemberId(
                    missionSymbolStackResponse.memberId(),
                    missionSymbolStackResponse.symbolStack());
        }
    }
}
