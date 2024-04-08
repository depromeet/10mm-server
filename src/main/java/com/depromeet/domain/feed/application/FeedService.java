package com.depromeet.domain.feed.application;

import com.depromeet.domain.feed.dto.response.FeedOneByProfileResponse;
import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.feed.dto.response.FeedSliceResponse;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.reaction.application.ReactionService;
import com.depromeet.global.util.MemberUtil;
import com.depromeet.global.util.SecurityUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: Redis 사용해서 캐싱 작업 필요
@Service
@RequiredArgsConstructor
@Transactional
public class FeedService {
    private final ReactionService reactionService;
    private final MemberUtil memberUtil;
    private final MissionRepository missionRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MemberRelationRepository memberRelationRepository;
    private final SecurityUtil securityUtil;
    private final MemberRepository memberRepository;

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
    public Slice<FeedOneResponse> findFeedV2(FeedVisibility visibility, Pageable pageable) {
        if (visibility == FeedVisibility.ALL) {
            return findAllFeedV2(pageable);
        }
        return findFollowerFeedV2(pageable);
    }

    public Slice<FeedOneResponse> findAllFeedV2(Pageable pageable) {
        return missionRecordRepository.findAllFetch(pageable).map(FeedOneResponse::from);
    }

    public Slice<FeedOneResponse> findFollowerFeedV2(Pageable pageable) {
        return null;
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

        sourceMembers.add(currentMember);
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
        List<MissionRecord> feedAllByMemberId =
                missionRecordRepository.findFeedAllByMemberId(targetId, visibilities);
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
        return records.stream().map(FeedOneByProfileResponse::from).toList();
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
