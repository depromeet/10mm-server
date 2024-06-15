package com.depromeet.domain.ranking.application;

import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.ranking.dao.RankingRepository;
import com.depromeet.domain.ranking.domain.Ranking;
import com.depromeet.domain.ranking.dto.RankingDto;
import com.depromeet.domain.ranking.dto.response.RankingResponse;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RankingService {

    private final RankingRepository rankingRepository;
    private final MissionRepository missionRepository;
    private final MissionService missionService;

    @Transactional(readOnly = true)
    public List<RankingResponse> findAllRanking() {
        List<Ranking> rankings = rankingRepository.findTop50ByOrderBySymbolStackDesc();
        return IntStream.range(0, rankings.size())
                .mapToObj(i -> RankingResponse.of(rankings.get(i), (long) i + 1))
                .collect(Collectors.toList());
    }

    public void updateSymbolStack(List<RankingDto> rankingDtos) {
        for (RankingDto rankingDto : rankingDtos) {
            Ranking ranking = Ranking.createRanking(rankingDto.symbolStack(), rankingDto.member());
            rankingRepository.updateSymbolStackAndMemberId(
                    ranking.getSymbolStack(), ranking.getMember().getId());
        }
    }

    @Transactional(readOnly = true)
    public List<RankingDto> findAllMissionSymbolStack() {
        List<Mission> missions = missionRepository.findAllMissionWithRecords();
        List<MissionRecord> completedMissionRecords =
                missionService.findCompletedMissionRecords(missions);

        return completedMissionRecords.stream()
                .collect(Collectors.groupingBy(MissionRecord::getMember))
                .entrySet()
                .stream()
                .map(
                        entry ->
                                RankingDto.of(
                                        entry.getKey(),
                                        missionService.symbolStackCalculate(entry.getValue())))
                .collect(Collectors.toList());
    }
}
