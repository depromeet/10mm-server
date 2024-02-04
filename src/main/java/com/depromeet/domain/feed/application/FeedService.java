package com.depromeet.domain.feed.application;

import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.global.util.MemberUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final MemberUtil memberUtil;
    private final MissionRepository missionRepository;
    private final MemberRelationRepository memberRelationRepository;
	private final MissionRecordRepository missionRecordRepository;

	public List<FeedOneResponse> findAllFeed() {
        final Member currentMember = memberUtil.getCurrentMember();
        List<Long> sourceIds =
                memberRelationRepository.findAllBySourceId(currentMember.getId()).stream()
                        .map(memberRelation -> memberRelation.getTarget().getId())
                        .toList();

        List<Mission> feedAll = missionRepository.findFeedAll(sourceIds);

		List<FeedOneResponse> returnList = feedAll.stream()
			.flatMap(mission -> {
				List<MissionRecord> missionRecords = mission.getMissionRecords();
				return missionRecords.stream()
					.filter(Objects::nonNull)
					.map(record -> FeedOneResponse.of(mission, record));
			})
			.toList();

		return returnList;
    }
}
