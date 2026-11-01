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
import com.depromeet.domain.notification.application.FcmService;
import com.depromeet.domain.notification.dao.NotificationRepository;
import com.depromeet.domain.notification.domain.Notification;
import com.depromeet.domain.notification.domain.NotificationType;
import com.depromeet.global.common.response.CursorSliceResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.CursorCodec;
import com.depromeet.global.util.MemberUtil;
import com.depromeet.global.util.SecurityUtil;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final FollowSummaryQueryService followSummaryQueryService;
    private final ApplicationEventPublisher eventPublisher;
    private final CursorCodec cursorCodec;
    private final SecurityUtil securityUtil;

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
                        NotificationType.FOLLOW,
                        currentMember,
                        targetMember,
                        currentMember.getId());
        notificationRepository.save(notification);
        memberRelationRepository.save(memberRelation);
        publishRelationChanged(currentMember.getId(), targetMember.getId());
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
        publishRelationChanged(currentMember.getId(), targetMember.getId());

        return optionalMemberRelation.isPresent()
                ? FollowerDeletedResponse.from(FollowStatus.FOLLOWED_BY_ME)
                : FollowerDeletedResponse.from(FollowStatus.NOT_FOLLOWING);
    }

    @Transactional(readOnly = true)
    public FollowFindTargetInfoResponse findTargetFollowInfo(Long targetId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Member targetMember = getTargetMember(targetId);

        FollowStatusProjection followStatusProjection =
                memberRelationRepository.findFollowStatus(
                        currentMember.getId(), targetMember.getId());
        FollowSummary followSummary = followSummaryQueryService.find(targetMember.getId());
        FollowStatus followStatus =
                determineFollowStatus(
                        followStatusProjection.getFollowing(),
                        followStatusProjection.getFollowedByMe());

        return FollowFindTargetInfoResponse.of(
                followSummary.followingCount(), followSummary.followerCount(), followStatus);
    }

    @Transactional(readOnly = true)
    public FollowFindMeInfoResponse findMeFollowInfo() {
        final Member currentMember = memberUtil.getCurrentMember();

        FollowSummary followSummary = followSummaryQueryService.find(currentMember.getId());
        return FollowFindMeInfoResponse.of(
                followSummary.followingCount(), followSummary.followerCount());
    }

    @Transactional(readOnly = true)
    public List<MemberFollowedResponse> findAllFollowedMember() {
        final Member currentMember = memberUtil.getCurrentMember();
        return memberRelationRepository.findAllFollowedMemberResponses(currentMember.getId());
    }

    private void validateSelfFollow(Long expectedId, Long actualId) {
        if (expectedId.equals(actualId)) {
            throw new CustomException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }
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

        // target 유저의 팔로잉
        List<Member> followingMembers =
                memberRelationRepository.findAllBySourceId(targetMember.getId()).stream()
                        .map(MemberRelation::getTarget)
                        .toList();

        // target 유저의 팔로워
        List<Member> followerMembers =
                memberRelationRepository.findAllByTargetId(targetMember.getId()).stream()
                        .map(MemberRelation::getSource)
                        .toList();

        List<Long> memberIds =
                Stream.concat(followingMembers.stream(), followerMembers.stream())
                        .map(Member::getId)
                        .distinct()
                        .toList();
        List<MemberRelationPair> relations =
                memberRelationRepository.findRelationsBetween(currentMember.getId(), memberIds);
        Set<Long> followingIds = MemberRelationPair.followingIds(relations, currentMember.getId());
        Set<Long> followerIds = MemberRelationPair.followerIds(relations, currentMember.getId());

        return FollowListResponse.of(
                targetMember.getProfile().getNickname(),
                toSearchResponses(followingMembers, followingIds, followerIds),
                toSearchResponses(followerMembers, followingIds, followerIds));
    }

    private static List<MemberSearchResponse> toSearchResponses(
            List<Member> members, Set<Long> followingIds, Set<Long> followerIds) {
        return members.stream()
                .map(member -> MemberSearchResponse.of(member, followingIds, followerIds))
                .toList();
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
        publishRelationChanged(targetMember.getId(), currentMember.getId());
        return optionalMemberRelation.isPresent()
                ? FollowerDeletedResponse.from(FollowStatus.FOLLOWING)
                : FollowerDeletedResponse.from(FollowStatus.NOT_FOLLOWING);
    }

    @Transactional(readOnly = true)
    public CursorSliceResponse<MemberSearchResponse> findFollowingPage(
            Long targetId, int size, String cursor) {
        return findFollowMemberPage(targetId, size, cursor, false);
    }

    @Transactional(readOnly = true)
    public CursorSliceResponse<MemberSearchResponse> findFollowerPage(
            Long targetId, int size, String cursor) {
        return findFollowMemberPage(targetId, size, cursor, true);
    }

    @Transactional(readOnly = true)
    public CursorSliceResponse<MemberSearchResponse> findMyFollowingPage(int size, String cursor) {
        return findFollowMemberPage(securityUtil.getCurrentMemberId(), size, cursor, false);
    }

    private CursorSliceResponse<MemberSearchResponse> findFollowMemberPage(
            Long targetId, int size, String cursor, boolean followerDirection) {
        LocalDateTime cursorCreatedAt = null;
        Long cursorRelationId = null;
        if (cursor != null) {
            List<String> values = cursorCodec.decode(cursor, 2);
            cursorCreatedAt = cursorCodec.dateTimeValue(values, 0);
            cursorRelationId = cursorCodec.longValue(values, 1);
        }

        List<FollowMemberPageRow> rows =
                followerDirection
                        ? memberRelationRepository.findFollowerPage(
                                targetId, cursorCreatedAt, cursorRelationId, size + 1)
                        : memberRelationRepository.findFollowingPage(
                                targetId, cursorCreatedAt, cursorRelationId, size + 1);
        boolean hasNext = rows.size() > size;
        if (hasNext) {
            rows = new ArrayList<>(rows.subList(0, size));
        }
        if (rows.isEmpty()) {
            getTargetMember(targetId);
        }

        Long viewerId = securityUtil.getCurrentMemberId();
        List<Long> memberIds = rows.stream().map(row -> row.member().getId()).toList();
        List<MemberRelationPair> relations =
                memberRelationRepository.findRelationsBetween(viewerId, memberIds);
        Set<Long> followingIds = MemberRelationPair.followingIds(relations, viewerId);
        Set<Long> followerIds = MemberRelationPair.followerIds(relations, viewerId);

        List<MemberSearchResponse> content =
                toSearchResponses(
                        rows.stream().map(FollowMemberPageRow::member).toList(),
                        followingIds,
                        followerIds);
        String nextCursor =
                hasNext && !rows.isEmpty()
                        ? cursorCodec.encode(
                                rows.getLast().createdAt(), rows.getLast().relationId())
                        : null;
        return CursorSliceResponse.of(content, nextCursor, hasNext);
    }

    private void publishRelationChanged(Long sourceMemberId, Long targetMemberId) {
        eventPublisher.publishEvent(new FollowRelationChangedEvent(sourceMemberId, targetMemberId));
    }
}
