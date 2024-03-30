package com.depromeet.domain.feed.application;

import static org.assertj.core.api.Assertions.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.comment.dao.CommentRepository;
import com.depromeet.domain.comment.domain.Comment;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

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
                        MissionVisibility.ALL,
                        LocalDateTime.of(2024, 3, 3, 0, 0),
                        LocalDateTime.of(2024, 3, 16, 0, 0),
                        LocalTime.of(12, 0, 0),
                        member3);

        missionRepository.save(mission1);
        missionRepository.save(mission2);
        missionRepository.save(mission3);

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

        missionRecordRepository.save(missionRecord1);
        missionRecordRepository.save(missionRecord2);
        missionRecordRepository.save(missionRecord3);

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

        reactionRepository.save(reactionToRecord1ByMember2);
        reactionRepository.save(reactionToRecord1ByMember3);
        reactionRepository.save(reactionToRecord2ByMember1);
        reactionRepository.save(reactionToRecord2ByMember3);
        reactionRepository.save(reactionToRecord3ByMember1);
        reactionRepository.save(reactionToRecord3ByMember3);

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

        commentRepository.save(commentToRecord1ByMember1);
        commentRepository.save(commentToRecord1ByMember2);
        commentRepository.save(commentToRecord1ByMember3);
        commentRepository.save(commentToRecord2ByMember1);
        commentRepository.save(commentToRecord2ByMember2);
        commentRepository.save(commentToRecord2ByMember3);
        commentRepository.save(commentToRecord3ByMember1);
        commentRepository.save(commentToRecord3ByMember2);
        commentRepository.save(commentToRecord3ByMember3);
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
