package com.depromeet.domain.follow.application;

import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.follow.dto.request.FollowCreateRequest;
import com.depromeet.domain.follow.dto.request.FollowDeleteRequest;
import com.depromeet.domain.follow.dto.response.FollowFindMeInfoResponse;
import com.depromeet.domain.follow.dto.response.FollowFindTargetInfoResponse;
import com.depromeet.domain.follow.dto.response.FollowStatus;
import com.depromeet.domain.follow.dto.response.FollowedMemberResponse;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {
    private final MemberUtil memberUtil;
    private final MemberRepository memberRepository;
    private final MemberRelationRepository memberRelationRepository;

    public void createFollow(FollowCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        Member targetMember = getTargetMember(request.targetId());

        boolean existMemberRelation =
                memberRelationRepository.existsBySourceIdAndTargetId(
                        currentMember.getId(), targetMember.getId());
        if (existMemberRelation) {
            throw new CustomException(ErrorCode.FOLLOW_ALREADY_EXIST);
        }

        MemberRelation memberRelation =
                MemberRelation.createMemberRelation(currentMember, targetMember);
        memberRelationRepository.save(memberRelation);
    }

    public void deleteFollow(FollowDeleteRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        Member targetMember = getTargetMember(request.targetId());

        MemberRelation memberRelation =
                memberRelationRepository
                        .findBySourceIdAndTargetId(currentMember.getId(), targetMember.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_EXIST));

        memberRelationRepository.delete(memberRelation);
    }

    @Transactional(readOnly = true)
    public FollowFindTargetInfoResponse findTargetFollowInfo(Long targetId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Member targetMember = getTargetMember(targetId);

        Long followingCount = memberRelationRepository.countBySourceId(targetMember.getId());
        Long followerCount = memberRelationRepository.countByTargetId(targetMember.getId());

        boolean isFollowing =
                memberRelationRepository.existsBySourceIdAndTargetId(
                        currentMember.getId(), targetMember.getId());
        boolean isFollowedByMe =
                memberRelationRepository.existsBySourceIdAndTargetId(
                        targetMember.getId(), currentMember.getId());
        FollowStatus followStatus = determineFollowStatus(isFollowing, isFollowedByMe);

        return FollowFindTargetInfoResponse.of(followingCount, followerCount, followStatus);
    }

    @Transactional(readOnly = true)
    public FollowFindMeInfoResponse findMeFollowInfo() {
        final Member currentMember = memberUtil.getCurrentMember();

        Long followingCount = memberRelationRepository.countBySourceId(currentMember.getId());
        Long followerCount = memberRelationRepository.countByTargetId(currentMember.getId());

        return FollowFindMeInfoResponse.of(followingCount, followerCount);
    }

    @Transactional(readOnly = true) // TODO: 로직 개선 필요
    public List<FollowedMemberResponse> findAllFollowedMember() {
        final Member currentMember = memberUtil.getCurrentMember();
        List<MemberRelation> followedMemberList =
                memberRelationRepository.findAllBySourceId(currentMember.getId());

        List<FollowedMemberResponse> result = new ArrayList<>();
        Map<Member, LocalDateTime> sortedByMemberMissionRecordMap = new HashMap<>();
        Map<Member, LocalDateTime> sortedByMemberRelationMap = new HashMap<>();

        for (MemberRelation memberRelation : followedMemberList) {
            Member targetMember = memberRelation.getTarget();
            List<Mission> targetMemberMissions = targetMember.getMissions();
            boolean isTargetMemberHasMissionTodayComplete = false;
            for (Mission mission : targetMemberMissions) {
                List<MissionRecord> targetMemberMissionRecords = mission.getMissionRecords();

                Optional<MissionRecord> optionalMissionRecord =
                        targetMemberMissionRecords.stream()
                                .filter(
                                        record ->
                                                record.getUploadStatus()
                                                                == ImageUploadStatus.COMPLETE
                                                        && isToday(record.getStartedAt()))
                                .max(Comparator.comparing(MissionRecord::getStartedAt));
                if (optionalMissionRecord.isPresent()) {
                    isTargetMemberHasMissionTodayComplete = true;
                    sortedByMemberMissionRecordMap.put(
                            targetMember, optionalMissionRecord.get().getStartedAt());
                    break;
                }
            }
            if (!isTargetMemberHasMissionTodayComplete) {
                sortedByMemberRelationMap.put(targetMember, memberRelation.getCreatedAt());
            }
        }
        sortedByMemberMissionRecordMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> result.add(FollowedMemberResponse.of(entry.getKey())));
        sortedByMemberRelationMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> result.add(FollowedMemberResponse.of(entry.getKey())));

        return result;
    }

    private boolean isToday(LocalDateTime dateTime) {
        LocalDateTime today = LocalDateTime.now();
        return dateTime.toLocalDate().isEqual(today.toLocalDate());
    }

    private FollowStatus determineFollowStatus(boolean isFollowing, boolean isFollowedByMe) {
        if (!isFollowing) {
            if (isFollowedByMe) {
                return FollowStatus.FOLLOWED_BY_ME;
            }
            return FollowStatus.NOT_FOLLOWING;
        }
        return FollowStatus.FOLLOWING;
    }

    private Member getTargetMember(Long targetId) {
        Member targetMember =
                memberRepository
                        .findById(targetId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                ErrorCode.FOLLOW_TARGET_MEMBER_NOT_FOUND));
        return targetMember;
    }
}
