package com.depromeet.domain.feed.application;

import com.depromeet.domain.comment.dao.CommentRepository;
import com.depromeet.domain.comment.dto.response.CommentDto;
import com.depromeet.domain.feed.domain.FeedVisibility;
import com.depromeet.domain.feed.dto.response.FeedOneByProfileResponse;
import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.feed.dto.response.FeedSliceResponse;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.reaction.dao.ReactionRepository;
import com.depromeet.domain.reaction.domain.Reaction;
import com.depromeet.domain.reaction.dto.response.ReactionGroupByEmojiResponse;
import com.depromeet.global.common.response.CursorSliceResponse;
import com.depromeet.global.util.CursorCodec;
import com.depromeet.global.util.MemberUtil;
import com.depromeet.global.util.SecurityUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: Redis 사용해서 캐싱 작업 필요
@Service
@RequiredArgsConstructor
@Transactional
public class FeedService {
    private final MemberUtil memberUtil;
    private final MissionRecordRepository missionRecordRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final MemberRelationRepository memberRelationRepository;
    private final SecurityUtil securityUtil;
    private final MemberRepository memberRepository;
    private final CursorCodec cursorCodec;

    @Deprecated
    @Transactional(readOnly = true)
    public List<FeedOneResponse> findAllFeedByVisibility(MissionVisibility visibilities) {
        if (visibilities == MissionVisibility.ALL) {
            final List<Member> members = memberRepository.findAll();
            return missionRecordRepository.findFeedByVisibility(members, List.of(visibilities));
        }

        final Member currentMember = memberUtil.getCurrentMember();
        List<Member> sourceMembers = getSourceMembers(currentMember.getId());

        sourceMembers.add(currentMember);
        return missionRecordRepository.findFeedAll(sourceMembers);
    }

    @Transactional(readOnly = true)
    public FeedSliceResponse findFeed(int size, Long lastId, MissionVisibility visibility) {
        if (visibility == MissionVisibility.ALL) {
            return findAllFeed(size, lastId);
        }
        return findFollowerFeed(size, lastId);
    }

    @Transactional(readOnly = true)
    public Slice<FeedOneResponse> findFeedV2(FeedVisibility visibility, int size, Long lastId) {
        // 전체
        if (visibility == FeedVisibility.ALL) {
            return findAllFeedV2(size, lastId);
        }
        // 팔로잉
        return findFollowingFeedV2(size, lastId);
    }

    private Slice<FeedOneResponse> findAllFeedV2(int size, Long lastId) {
        return enrichInteractions(missionRecordRepository.findAllFetch(size, lastId));
    }

    private Slice<FeedOneResponse> findFollowingFeedV2(int size, Long lastId) {
        final Long currentMemberId = securityUtil.getCurrentMemberId();

        return enrichInteractions(
                missionRecordRepository.findAllFetchByFollowings(size, lastId, currentMemberId));
    }

    private Slice<FeedOneResponse> enrichInteractions(Slice<FeedOneResponse> feedSlice) {
        List<Long> missionRecordIds =
                feedSlice.getContent().stream().map(FeedOneResponse::recordId).toList();
        if (missionRecordIds.isEmpty()) {
            return feedSlice;
        }

        Map<Long, List<CommentDto>> commentsByRecordId =
                commentRepository
                        .findAllByMissionRecordIdInOrderByCreatedAtAsc(missionRecordIds)
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        comment -> comment.getMissionRecord().getId(),
                                        Collectors.mapping(CommentDto::from, Collectors.toList())));
        Map<Long, List<Reaction>> reactionsByRecordId =
                reactionRepository.findAllWithMemberByMissionRecordIdIn(missionRecordIds).stream()
                        .collect(
                                Collectors.groupingBy(
                                        reaction -> reaction.getMissionRecord().getId()));

        return feedSlice.map(
                feed ->
                        feed.withInteractions(
                                ReactionGroupByEmojiResponse.groupByEmojiType(
                                        reactionsByRecordId.getOrDefault(
                                                feed.recordId(), List.of())),
                                commentsByRecordId.getOrDefault(feed.recordId(), List.of())));
    }

    // 전체 피드 탭
    public FeedSliceResponse findAllFeed(int size, Long lastId) {
        final List<Member> members = memberRepository.findAll();
        Slice<FeedOneResponse> feedByVisibilityAndPage =
                missionRecordRepository.findFeedByVisibilityAndPage(
                        size, lastId, members, List.of(MissionVisibility.ALL));
        return FeedSliceResponse.from(feedByVisibilityAndPage);
    }

    // 팔로워 피드 탭
    public FeedSliceResponse findFollowerFeed(int size, Long lastId) {
        final Member currentMember = memberUtil.getCurrentMember();
        List<Member> sourceMembers = getSourceMembers(currentMember.getId());

        Slice<FeedOneResponse> feedAllByPage =
                missionRecordRepository.findFeedAllByPage(size, lastId, sourceMembers);
        return FeedSliceResponse.from(feedAllByPage);
    }

    @Transactional(readOnly = true)
    public List<FeedOneResponse> findAllFeed() {
        final Member currentMember = memberUtil.getCurrentMember();
        List<Member> members = getSourceMembers(currentMember.getId());

        members.add(currentMember);
        return missionRecordRepository.findFeedAll(members);
    }

    @Transactional(readOnly = true)
    public List<FeedOneByProfileResponse> findAllFeedByTargetId(Long targetId) {
        final Long sourceId = securityUtil.getCurrentMemberId();

        if (isMyFeedRequired(targetId, sourceId)) {
            return findFeedByOtherMember(sourceId, targetId);
        }
        return findFeedByCurrentMember(sourceId);
    }

    @Transactional(readOnly = true)
    public CursorSliceResponse<FeedOneByProfileResponse> findProfileFeedV2(
            Long targetId, int size, String cursor) {
        memberUtil.getMemberByMemberId(targetId);
        Long viewerId = securityUtil.getCurrentMemberId();
        Long lastId =
                cursor == null ? null : cursorCodec.longValue(cursorCodec.decode(cursor, 1), 0);
        List<FeedOneByProfileResponse> rows =
                missionRecordRepository.findProfileFeedPage(targetId, viewerId, lastId, size + 1);
        boolean hasNext = rows.size() > size;
        if (hasNext) {
            rows = new ArrayList<>(rows.subList(0, size));
        }
        String nextCursor =
                hasNext && !rows.isEmpty() ? cursorCodec.encode(rows.getLast().recordId()) : null;
        return CursorSliceResponse.of(rows, nextCursor, hasNext);
    }

    private List<Member> getSourceMembers(Long currentMemberId) {
        return memberRelationRepository.findAllBySourceId(currentMemberId).stream()
                .map(MemberRelation::getTarget)
                .toList();
    }

    private boolean isMyFeedRequired(Long targetId, Long sourceId) {
        return !targetId.equals(sourceId);
    }

    private List<FeedOneByProfileResponse> findFeedByOtherMember(Long sourceId, Long targetId) {
        final Member targetMember = memberUtil.getMemberByMemberId(targetId);

        // 팔로우 관계 true: visibility.FOLLOW and ALL, false: visibility.ALL only
        boolean isMemberRelationExistsWithMe =
                memberRelationRepository.existsBySourceIdAndTargetId(
                        sourceId, targetMember.getId());
        List<MissionVisibility> visibilities =
                determineVisibilityConditionsByRelationsWithMe(isMemberRelationExistsWithMe);
        return missionRecordRepository.findFeedAllByMemberId(targetId, visibilities);
    }

    private List<FeedOneByProfileResponse> findFeedByCurrentMember(Long sourceId) {
        List<MissionVisibility> visibilities =
                List.of(MissionVisibility.NONE, MissionVisibility.FOLLOWER, MissionVisibility.ALL);
        return missionRecordRepository.findFeedAllByMemberId(sourceId, visibilities);
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
