package com.depromeet.domain.follow.application;

import static com.depromeet.global.common.constants.PushNotificationConstants.*;

import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.follow.dto.request.FollowCreateRequest;
import com.depromeet.domain.follow.dto.request.FollowDeleteRequest;
import com.depromeet.domain.follow.dto.response.*;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.dto.response.MemberSearchResponse;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.missionRecord.domain.ImageUploadStatus;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.notification.application.FcmService;
import com.depromeet.domain.notification.dao.NotificationRepository;
import com.depromeet.domain.notification.domain.Notification;
import com.depromeet.domain.notification.domain.NotificationType;
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
    private final NotificationRepository notificationRepository;
    private final MemberUtil memberUtil;
    private final MemberRepository memberRepository;
    private final MemberRelationRepository memberRelationRepository;
    private final FcmService fcmService;

    public void createFollow(FollowCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        validateSelfFollow(currentMember.getId(), request.targetId());
        Member targetMember = getTargetMember(request.targetId());

        boolean existMemberRelation =
                memberRelationRepository.existsBySourceIdAndTargetId(
                        currentMember.getId(), targetMember.getId());
        if (existMemberRelation) {
            throw new CustomException(ErrorCode.FOLLOW_ALREADY_EXIST);
        }

        MemberRelation memberRelation =
                MemberRelation.createMemberRelation(currentMember, targetMember);

        fcmService.sendMessageSync(
                targetMember.getFcmInfo().getFcmToken(),
                PUSH_SERVICE_TITLE,
                String.format(PUSH_SERVICE_CONTENT, currentMember.getProfile().getNickname()));
        Notification notification =
                Notification.createNotification(
                        NotificationType.FOLLOW, currentMember, targetMember);
        notificationRepository.save(notification);
        memberRelationRepository.save(memberRelation);
    }

    public FollowerDeletedResponse deleteFollow(FollowDeleteRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        Member targetMember = getTargetMember(request.targetId());

        MemberRelation memberRelation =
                memberRelationRepository
                        .findBySourceIdAndTargetId(currentMember.getId(), targetMember.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_EXIST));

        Optional<Notification> optionalNotification =
                notificationRepository.findBySourceMemberIdAndTargetMemberIdAndNotificationType(
                        currentMember.getId(), targetMember.getId(), NotificationType.FOLLOW);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notificationRepository.delete(notification);
        }
        memberRelationRepository.delete(memberRelation);

        Optional<MemberRelation> optionalMemberRelation =
                memberRelationRepository.findBySourceIdAndTargetId(
                        targetMember.getId(), currentMember.getId());

        return optionalMemberRelation.isPresent()
                ? FollowerDeletedResponse.from(FollowStatus.FOLLOWED_BY_ME)
                : FollowerDeletedResponse.from(FollowStatus.NOT_FOLLOWING);
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
    public List<MemberFollowedResponse> findAllFollowedMember() {
        final Member currentMember = memberUtil.getCurrentMember();
        List<MemberRelation> followedMemberList =
                memberRelationRepository.findAllBySourceId(currentMember.getId());

        // 결과를 반환하는 List
        List<MemberFollowedResponse> result = new ArrayList<>();

        // 당일 미션 기록이 존재하여 미션기록의 순서로 정렬이 될 회원들이 담길 Map
        Map<Member, LocalDateTime> sortedByMemberMissionRecordMap = new HashMap<>();

        // 당일 미션 기록이 존재하지 않아서 팔로우 시간 순서로 정렬이 될 회원들이 담길 Map
        Map<Member, LocalDateTime> sortedByMemberRelationMap = new HashMap<>();

        for (MemberRelation memberRelation : followedMemberList) {
            Member targetMember = memberRelation.getTarget();
            List<Mission> targetMemberMissions = targetMember.getMissions();
            boolean isTargetMemberHasMissionTodayComplete = false;
            for (Mission mission : targetMemberMissions) {
                List<MissionRecord> targetMemberMissionRecords = mission.getMissionRecords();

                // 완료한 미션기록이고 오늘인 미션기록 찾기
                Optional<MissionRecord> optionalMissionRecord =
                        targetMemberMissionRecords.stream()
                                .filter(
                                        record ->
                                                record.getUploadStatus()
                                                                == ImageUploadStatus.COMPLETE
                                                        && isToday(record.getStartedAt()))
                                .max(Comparator.comparing(MissionRecord::getStartedAt));

                // 있다면 미션 기록들 중에서 시간비교를 해야하기 때문에 sortedByMemberMissionRecordMap에 저장
                if (optionalMissionRecord.isPresent()) {
                    isTargetMemberHasMissionTodayComplete = true;
                    sortedByMemberMissionRecordMap.put(
                            targetMember, optionalMissionRecord.get().getStartedAt());
                    break;
                }
            }

            // 없다면 팔로우 시간 순서로 비교 해야하기 때문에 sortedByMemberRelationMap에 저장
            if (!isTargetMemberHasMissionTodayComplete) {
                sortedByMemberRelationMap.put(targetMember, memberRelation.getCreatedAt());
            }
        }

        // (정렬조건 1번째) 당일 미션 기록이 존재하는 회원들 먼저 result에 저장
        sortedByMemberMissionRecordMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> result.add(MemberFollowedResponse.of(entry.getKey())));

        // (정렬조건 2번째) 미션 기록이 존재하지 않는 회원들은 팔로우 시간으로 result에 저장
        sortedByMemberRelationMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> result.add(MemberFollowedResponse.of(entry.getKey())));

        return result;
    }

    private void validateSelfFollow(Long expectedId, Long actualId) {
        if (expectedId.equals(actualId)) {
            throw new CustomException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }
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

    @Transactional(readOnly = true)
    public FollowListResponse findFollowList(Long targetId) {
        final Member currentMember = memberUtil.getCurrentMember();
        Member targetMember = getTargetMember(targetId);

        List<MemberSearchResponse> followingList = new ArrayList<>();
        List<MemberSearchResponse> followerList = new ArrayList<>();

        List<MemberRelation> targetMemberSources =
                memberRelationRepository.findAllBySourceId(targetMember.getId());
        List<MemberRelation> targetMemberTargets =
                memberRelationRepository.findAllByTargetId(targetMember.getId());

        List<MemberRelation> currentMemberSources =
                memberRelationRepository.findAllBySourceId(currentMember.getId());
        List<MemberRelation> currentMemberTargets =
                memberRelationRepository.findAllByTargetId(currentMember.getId());

        // target 유저의 팔로잉
        List<Member> followingMembers =
                targetMemberSources.stream().map(MemberRelation::getTarget).toList();

        // target 유저의 팔로워
        List<Member> followerMembers =
                targetMemberTargets.stream().map(MemberRelation::getSource).toList();

        // 팔로잉 리스트 구하기
        getFollowStatusIncludeList(
                followingMembers, currentMemberSources, followingList, currentMemberTargets);

        // 팔로워 리스트 구하기
        getFollowStatusIncludeList(
                followerMembers, currentMemberSources, followerList, currentMemberTargets);

        return FollowListResponse.of(
                targetMember.getProfile().getNickname(), followingList, followerList);
    }

    public FollowerDeletedResponse deleteFollower(Long targetId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Member targetMember = getTargetMember(targetId);

        MemberRelation memberRelation =
                memberRelationRepository
                        .findBySourceIdAndTargetId(targetMember.getId(), currentMember.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_EXIST));
        memberRelationRepository.delete(memberRelation);

        Optional<MemberRelation> optionalMemberRelation =
                memberRelationRepository.findBySourceIdAndTargetId(
                        currentMember.getId(), targetMember.getId());
        return optionalMemberRelation.isPresent()
                ? FollowerDeletedResponse.from(FollowStatus.FOLLOWING)
                : FollowerDeletedResponse.from(FollowStatus.NOT_FOLLOWING);
    }

    /**
     * 특정 멤버가 팔로우 중인 멤버 목록을 조회합니다.
     *
     * @param source 특정 멤버
     * @return 팔로우 중인 멤버 목록
     */
    public List<Member> getFollowingMembers(Member source) {
        return memberRelationRepository.findBySource(source).stream()
                .map(MemberRelation::getTarget)
                .toList();
    }

    private static void getFollowStatusIncludeList(
            List<Member> targetMembers,
            List<MemberRelation> currentMemberSources,
            List<MemberSearchResponse> resultList,
            List<MemberRelation> currentMemberTargets) {
        for (Member member : targetMembers) {
            boolean existRelation = false;
            for (MemberRelation memberRelation : currentMemberSources) {
                if (member.getId().equals(memberRelation.getTarget().getId())) {
                    existRelation = true;
                    break;
                }
            }

            if (existRelation) { // 조회 된 애들 중 내가 팔로우한 애라면
                resultList.add(MemberSearchResponse.toFollowingResponse(member));
                continue;
            }

            // 내가 팔로우를 하지 않았을 때
            Optional<MemberRelation> optionalMemberRelation =
                    currentMemberTargets.stream()
                            .filter(
                                    memberRelation ->
                                            member.getId()
                                                    .equals(memberRelation.getSource().getId()))
                            .findFirst();
            if (optionalMemberRelation.isPresent()) { // 상대방만 나를 팔로우 하고 있을  때
                resultList.add(MemberSearchResponse.toFollowedByMeResponse(member));
                continue;
            }

            // 아니라면 서로 팔로우가 아닌 상태
            resultList.add(MemberSearchResponse.toNotFollowingResponse(member));
        }
    }
}
