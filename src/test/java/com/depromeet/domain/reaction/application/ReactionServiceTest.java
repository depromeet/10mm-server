package com.depromeet.domain.reaction.application;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.DatabaseCleaner;
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
import com.depromeet.domain.reaction.dto.ReactionCreateRequest;
import com.depromeet.domain.reaction.dto.ReactionCreateResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.security.PrincipalDetails;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ReactionServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 2, 7, 5, 0, 0);

    @Autowired private ReactionService reactionService;
    @Autowired private ReactionRepository reactionRepository;
    @Autowired private DatabaseCleaner databaseCleaner;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MissionRepository missionRepository;
    @Autowired private MissionRecordRepository missionRecordRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    private Member saveAndRegisterMember() {
        OauthInfo oauthInfo =
                OauthInfo.createOauthInfo("testOauthId", "testOauthProvider", "testOauthEmail");
        Member member = Member.createNormalMember(oauthInfo, "testNickname");
        memberRepository.save(member);
        PrincipalDetails principalDetails = new PrincipalDetails(member.getId(), "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return member;
    }

    private void createMissionAndMissionRecord(Member member) {
        Mission mission =
                Mission.createMission(
                        "testMission",
                        "testDescription",
                        1,
                        MissionCategory.PROJECT,
                        MissionVisibility.ALL,
                        NOW.minusDays(5),
                        NOW.plusDays(5),
                        member);
        missionRepository.save(mission);
        MissionRecord missionRecord =
                MissionRecord.createMissionRecord(
                        Duration.ofMinutes(30), NOW.minusMinutes(25), NOW.minusMinutes(5), mission);
        missionRecordRepository.save(missionRecord);
    }

    @Nested
    class 리액션_생성시 {

        @Test
        void 내_미션기록에_추가하면_실패한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);

            ReactionCreateRequest request = new ReactionCreateRequest(1L, EmojiType.PURPLE_HEART);

            // when & then
            assertThrows(
                    CustomException.class,
                    () -> reactionService.createReaction(request),
                    ErrorCode.REACTION_SELF_NOT_ALLOWED.getMessage());
        }

        @Test
        void 타인의_미션기록에_추가하면_성공한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);

            SecurityContextHolder.clearContext(); // 현재 회원 로그아웃

            Member otherMember = saveAndRegisterMember(); // 다른 회원 로그인
            createMissionAndMissionRecord(otherMember);

            ReactionCreateRequest request = new ReactionCreateRequest(1L, EmojiType.PURPLE_HEART);

            // when
            ReactionCreateResponse response = reactionService.createReaction(request);

            // then
            assertNotNull(response);
            assertEquals(1L, response.reactionId());
        }

        @Test
        void 이미_리액션을_남긴_미션기록에_리액션을_추가하면_실패한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);
            ReactionCreateRequest request = new ReactionCreateRequest(1L, EmojiType.PURPLE_HEART);
            reactionService.createReaction(request);

            // when, then
            assertThrows(
                    CustomException.class,
                    () -> reactionService.createReaction(request),
                    ErrorCode.REACTION_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    class 리액션_삭제시 {

        @Test
        void 성공한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);
            ReactionCreateRequest request = new ReactionCreateRequest(1L, EmojiType.PURPLE_HEART);
            ReactionCreateResponse response = reactionService.createReaction(request);

            // when
            reactionService.deleteReaction(response.reactionId());

            // then
            assertTrue(reactionRepository.findById(response.reactionId()).isEmpty());
        }

        @Test
        void 자신의_리액션이_아니면_실패한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);
            ReactionCreateRequest request = new ReactionCreateRequest(1L, EmojiType.PURPLE_HEART);
            ReactionCreateResponse response =
                    reactionService.createReaction(request); // 현재 회원이 리액션을 추가
            Long reactionId = response.reactionId();

            SecurityContextHolder.clearContext(); // 현재 회원 로그아웃

            saveAndRegisterMember(); // 다른 회원 로그인

            // when, then
            assertThrows(
                    CustomException.class,
                    () -> reactionService.deleteReaction(reactionId),
                    ErrorCode.REACTION_MEMBER_MISMATCH.getMessage());
        }
    }
}
