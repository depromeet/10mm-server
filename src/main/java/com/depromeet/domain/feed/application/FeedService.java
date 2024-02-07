package com.depromeet.domain.feed.application;

import com.depromeet.domain.feed.dto.response.FeedOneByProfileResponse;
import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.global.util.MemberUtil;
import com.depromeet.global.util.SecurityUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: Redis 사용해서 캐싱 작업 필요
@Service
@RequiredArgsConstructor
@Transactional
public class FeedService {
    private final MemberUtil memberUtil;
    private final MissionRecordRepository missionRecordRepository;
    private final MemberRelationRepository memberRelationRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public List<FeedOneResponse> findAllFeed() {
        final Member currentMember = memberUtil.getCurrentMember();
        List<Member> members =
                memberRelationRepository.findAllBySourceId(currentMember.getId()).stream()
                        .map(MemberRelation::getTarget)
                        .collect(Collectors.toList());
        members.add(currentMember);

        return missionRecordRepository.findFeedAll(members);
    }

    @Transactional(readOnly = true)
    public List<FeedOneByProfileResponse> findAllFeedByTargetId(Long targetId) {
        final Long sourceId = securityUtil.getCurrentMemberId();

        if (!isMyFeedRequired(targetId, sourceId)) {
            return findFeedByOtherMember(sourceId, targetId);
        }
        return findFeedByCurrentMember(sourceId);
    }

    private boolean isMyFeedRequired(Long targetId, Long sourceId) {
        return targetId.equals(sourceId);
    }

    private List<FeedOneByProfileResponse> findFeedByOtherMember(Long sourceId, Long targetId) {
        final Member targetMember = memberUtil.getMemberByMemberId(targetId);

        // 팔로우 관계 true: visibility.FOLLOW and ALL, false: visibility.ALL only
        boolean isMemberRelationExistsWithMe =
                memberRelationRepository.existsBySourceIdAndTargetId(
                        sourceId, targetMember.getId());

        List<MissionRecord> feedAllByMemberId =
                missionRecordRepository.findFeedAllByMemberId(
                        targetId,
                        determineVisibilityConditionsByRelationsWithMe(
                                isMemberRelationExistsWithMe));
        return extractFeedResponses(feedAllByMemberId);
    }

    private List<FeedOneByProfileResponse> findFeedByCurrentMember(Long sourceId) {
        List<MissionVisibility> visibilities =
                List.of(MissionVisibility.NONE, MissionVisibility.FOLLOWER, MissionVisibility.ALL);
        List<MissionRecord> feedAllByMemberId =
                missionRecordRepository.findFeedAllByMemberId(sourceId, visibilities);
        return extractFeedResponses(feedAllByMemberId);
    }

    private List<FeedOneByProfileResponse> extractFeedResponses(List<MissionRecord> records) {
        return records.stream()
                .map(record -> FeedOneByProfileResponse.of(record.getMission(), record))
                .toList();
    }

    private List<MissionVisibility> determineVisibilityConditionsByRelationsWithMe(
            boolean isMemberRelationExistsWithMe) {
        List<MissionVisibility> visibilities = new ArrayList<>();
        visibilities.add(MissionVisibility.ALL);

        if (isMemberRelationExistsWithMe) {
            visibilities.add(MissionVisibility.FOLLOWER);
        }
        return visibilities;
    }
}
