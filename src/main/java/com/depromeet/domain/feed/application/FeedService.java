package com.depromeet.domain.feed.application;

import com.depromeet.domain.feed.dto.response.FeedOneByProfileResponse;
import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.global.util.MemberUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final MemberUtil memberUtil;
    private final MissionRepository missionRepository;
    private final MemberRelationRepository memberRelationRepository;

    public List<FeedOneResponse> findAllFeed() {
        final Member currentMember = memberUtil.getCurrentMember();
        List<Long> sourceIds =
                new ArrayList<>(
                        memberRelationRepository.findAllBySourceId(currentMember.getId()).stream()
                                .map(memberRelation -> memberRelation.getTarget().getId())
                                .toList());
        sourceIds.add(currentMember.getId());

        List<Mission> feedAll = missionRepository.findFeedAll(sourceIds);

        return feedAll.stream()
                .flatMap(
                        mission -> {
                            List<MissionRecord> missionRecords = mission.getMissionRecords();
                            return missionRecords.stream()
                                    .filter(Objects::nonNull)
                                    .map(record -> FeedOneResponse.of(mission, record));
                        })
                .toList();
    }

    public List<FeedOneByProfileResponse> findAllFeedByTargetId(Long targetId) {
        Member sourceMember = memberUtil.getCurrentMember();

        /*
         본인 프로필
         targetId가 본인이다.
         그렇다면 공개 여부 상관없이 full-scan

         타인 프로필
         만약 sourceId와 targetId가 다르다.
         그렇다면 공개 여부에서 비공개는 exclude 한다.
         추가로 내가 타인 프로필에서 팔로잉 관계가 이뤄졌는지 여부 확인하여 팔로우 관계 여부 체크
        */
        if (targetId != null) {
            return findFeedByTargetMember(sourceMember, targetId);
        } else {
            return findFeedBySourceMember(sourceMember);
        }
    }

    private List<FeedOneByProfileResponse> findFeedByTargetMember(
            Member sourceMember, Long targetId) {
        final Member targetMember = memberUtil.getMemberByMemberId(targetId);
        List<MissionVisibility> visibilities =
                new ArrayList<>(Arrays.asList(MissionVisibility.ALL));

        // 팔로우 관계 true: visibility.FOLLOW and ALL, false: visibility.ALL only
        boolean existMemberRelation =
                memberRelationRepository.existsBySourceIdAndTargetId(
                        sourceMember.getId(), targetMember.getId());
        if (existMemberRelation) {
            visibilities.add(MissionVisibility.FOLLOWER);
        }
        if (sourceMember.getId().equals(targetId)) {
            visibilities.add(MissionVisibility.FOLLOWER);
            visibilities.add(MissionVisibility.NONE);
        }
        List<Mission> feedAllByMemberId =
                missionRepository.findFeedAllByMemberId(targetId, visibilities);
        return extractFeedResponses(feedAllByMemberId);
    }

    private List<FeedOneByProfileResponse> findFeedBySourceMember(Member sourceMember) {
        List<Mission> feedAllByMemberId =
                missionRepository.findFeedAllByMemberId(sourceMember.getId(), null);
        return extractFeedResponses(feedAllByMemberId);
    }

    private List<FeedOneByProfileResponse> extractFeedResponses(List<Mission> missions) {
        return missions.stream()
                .flatMap(
                        mission -> {
                            List<MissionRecord> missionRecords = mission.getMissionRecords();
                            return missionRecords.stream()
                                    .filter(Objects::nonNull)
                                    .map(record -> FeedOneByProfileResponse.of(mission, record));
                        })
                .toList();
    }
}
