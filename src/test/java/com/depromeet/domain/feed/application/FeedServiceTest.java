package com.depromeet.domain.feed.application;

import static org.assertj.core.api.Assertions.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.comment.dao.CommentRepository;
import com.depromeet.domain.comment.domain.Comment;
import com.depromeet.domain.feed.domain.FeedVisibility;
import com.depromeet.domain.feed.dto.response.FeedOneResponse;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.reaction.dao.ReactionRepository;
import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import com.depromeet.global.security.PrincipalDetails;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FeedServiceTest {

    @Autowired private FeedService feedService;
    @Autowired private DatabaseCleaner databaseCleaner;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MissionRepository missionRepository;
    @Autowired private MissionRecordRepository missionRecordRepository;
    @Autowired private ReactionRepository reactionRepository;
    @Autowired private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    private void logoutAndReloginAs(Long memberId) {
        SecurityContextHolder.clearContext(); // 현재 회원 로그아웃
        PrincipalDetails principalDetails = new PrincipalDetails(memberId, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setFixture() {
        Member member1 =
                Member.createNormalMember(
                        OauthInfo.createOauthInfo("test1", "test1", "test1"), "test1");
        Member member2 =
                Member.createNormalMember(
                        OauthInfo.createOauthInfo("test2", "test2", "test2"), "test2");
        Member member3 =
                Member.createNormalMember(
                        OauthInfo.createOauthInfo("test3", "test3", "test3"), "test3");

        memberRepository.saveAll(List.of(member1, member2, member3));

        Mission mission1 =
                Mission.createMission(
                        "mission1",
                        "content1",
                        1,
                        MissionCategory.PROJECT,
                        MissionVisibility.ALL,
                        LocalDateTime.of(2024, 3, 1, 0, 0),
                        LocalDateTime.of(2024, 3, 14, 0, 0),
                        LocalTime.of(12, 0, 0),
                        member1);

        Mission mission2 =
                Mission.createMission(
                        "mission2",
                        "content2",
                        2,
                        MissionCategory.EXERCISE,
                        MissionVisibility.ALL,
                        LocalDateTime.of(2024, 3, 2, 0, 0),
                        LocalDateTime.of(2024, 3, 15, 0, 0),
                        LocalTime.of(12, 0, 0),
                        member2);

        Mission mission3 =
                Mission.createMission(
                        "mission3",
                        "content3",
                        3,
                        MissionCategory.STUDY,
                        MissionVisibility.FOLLOWER,
                        LocalDateTime.of(2024, 3, 3, 0, 0),
                        LocalDateTime.of(2024, 3, 16, 0, 0),
                        LocalTime.of(12, 0, 0),
                        member3);

        Mission mission4 =
                Mission.createMission(
                        "mission4",
                        "content4",
                        4,
                        MissionCategory.ETC,
                        MissionVisibility.NONE,
                        LocalDateTime.of(2024, 3, 4, 0, 0),
                        LocalDateTime.of(2024, 3, 17, 0, 0),
                        LocalTime.of(12, 0, 0),
                        member3);

        missionRepository.saveAll(List.of(mission1, mission2, mission3, mission4));

        MissionRecord missionRecord1 =
                MissionRecord.createMissionRecord(
                        Duration.ofMinutes(15),
                        LocalDateTime.of(2024, 3, 1, 12, 0),
                        LocalDateTime.of(2024, 3, 1, 12, 15),
                        mission1);

        MissionRecord missionRecord2 =
                MissionRecord.createMissionRecord(
                        Duration.ofMinutes(25),
                        LocalDateTime.of(2024, 3, 2, 12, 0),
                        LocalDateTime.of(2024, 3, 2, 12, 25),
                        mission2);

        MissionRecord missionRecord3 =
                MissionRecord.createMissionRecord(
                        Duration.ofMinutes(35),
                        LocalDateTime.of(2024, 3, 3, 12, 0),
                        LocalDateTime.of(2024, 3, 3, 12, 35),
                        mission3);

        MissionRecord missionRecord4 =
                MissionRecord.createMissionRecord(
                        Duration.ofMinutes(45),
                        LocalDateTime.of(2024, 3, 4, 12, 0),
                        LocalDateTime.of(2024, 3, 4, 12, 45),
                        mission4);

        missionRecordRepository.saveAll(
                List.of(missionRecord1, missionRecord2, missionRecord3, missionRecord4));

        Reaction reactionToRecord1ByMember2 =
                Reaction.createReaction(EmojiType.BLUE_HEART, member2, missionRecord1);
        Reaction reactionToRecord1ByMember3 =
                Reaction.createReaction(EmojiType.THUMBS_UP, member3, missionRecord1);

        Reaction reactionToRecord2ByMember1 =
                Reaction.createReaction(EmojiType.FIRE, member1, missionRecord2);
        Reaction reactionToRecord2ByMember3 =
                Reaction.createReaction(EmojiType.PARTY_POPPER, member3, missionRecord2);

        Reaction reactionToRecord3ByMember1 =
                Reaction.createReaction(EmojiType.UNICORN, member1, missionRecord3);
        Reaction reactionToRecord3ByMember3 =
                Reaction.createReaction(EmojiType.PARTYING_FACE, member3, missionRecord3);

        reactionRepository.saveAll(
                List.of(
                        reactionToRecord1ByMember2,
                        reactionToRecord1ByMember3,
                        reactionToRecord2ByMember1,
                        reactionToRecord2ByMember3,
                        reactionToRecord3ByMember1,
                        reactionToRecord3ByMember3));

        Comment commentToRecord1ByMember1 =
                Comment.createComment("commentToRecord1ByMember1", member1, missionRecord1);
        Comment commentToRecord1ByMember2 =
                Comment.createComment("commentToRecord1ByMember2", member2, missionRecord1);
        Comment commentToRecord1ByMember3 =
                Comment.createComment("commentToRecord1ByMember3", member3, missionRecord1);

        Comment commentToRecord2ByMember1 =
                Comment.createComment("commentToRecord2ByMember1", member1, missionRecord2);
        Comment commentToRecord2ByMember2 =
                Comment.createComment("commentToRecord2ByMember2", member2, missionRecord2);
        Comment commentToRecord2ByMember3 =
                Comment.createComment("commentToRecord2ByMember3", member3, missionRecord2);

        Comment commentToRecord3ByMember1 =
                Comment.createComment("commentToRecord3ByMember1", member1, missionRecord3);
        Comment commentToRecord3ByMember2 =
                Comment.createComment("commentToRecord3ByMember2", member2, missionRecord3);
        Comment commentToRecord3ByMember3 =
                Comment.createComment("commentToRecord3ByMember3", member3, missionRecord3);

        commentRepository.saveAll(
                List.of(
                        commentToRecord1ByMember1,
                        commentToRecord1ByMember2,
                        commentToRecord1ByMember3,
                        commentToRecord2ByMember1,
                        commentToRecord2ByMember2,
                        commentToRecord2ByMember3,
                        commentToRecord3ByMember1,
                        commentToRecord3ByMember2,
                        commentToRecord3ByMember3));
    }
    }

    @Nested
    class 피드_V2_조회_테스트 {

        @Test
        void 전체_피드_조회하기() {
            // given
            setFixture();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Slice<FeedOneResponse> response = feedService.findAllFeedV2(pageable);

            // then
            assertThat(response.getContent()).hasSize(3);
        }
    }
}
