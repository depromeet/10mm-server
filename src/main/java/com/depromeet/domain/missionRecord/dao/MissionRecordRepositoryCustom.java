package com.depromeet.domain.missionRecord.dao;

import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import java.time.YearMonth;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MissionRecordRepositoryCustom {

    List<MissionRecord> findAllByMissionIdAndYearMonth(Long missionId, YearMonth yearMonth);

    List<MissionRecord> findAllByCompletedMission(Long missionId);

    List<FeedOneResponse> findFeedAll(List<Member> members);

    List<FeedOneResponse> findFeedByVisibility(
            List<Member> members, List<MissionVisibility> visibility);

    List<MissionRecord> findFeedAllByMemberId(Long memberId, List<MissionVisibility> visibilities);

    boolean isCompletedMissionExistsToday(Long missionId);

    void deleteByMissionRecordId(Long missionRecordId);

    Slice<FeedOneResponse> findFeedAllByPage(int size, Long lastId, List<Member> members);

    Slice<FeedOneResponse> findFeedByVisibilityAndPage(
            int size, Long lastId, List<Member> members, List<MissionVisibility> visibility);

    Slice<MissionRecord> findAllFetch(Pageable pageable);

    Slice<MissionRecord> findAllFetchByFollowings(Pageable pageable, List<Member> followingMembers);
}
