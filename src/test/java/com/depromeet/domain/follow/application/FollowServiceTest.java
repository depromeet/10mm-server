package com.depromeet.domain.follow.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.NoTransactionExtension;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.follow.dto.request.FollowCreateRequest;
import com.depromeet.domain.follow.dto.request.FollowDeleteRequest;
import com.depromeet.domain.follow.dto.response.*;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.security.PrincipalDetails;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(NoTransactionExtension.class)
class FollowServiceTest {
    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberRelationRepository memberRelationRepository;
    @Autowired private MissionRepository missionRepository;
    @Autowired private MissionRecordRepository missionRecordRepository;
    @Autowired private FollowService followService;

    @BeforeEach
    void setUp() {
        PrincipalDetails principal = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, "password", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    class 팔로우를_추가할_때 {

        @Test
        void 로그인된_회원이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            FollowCreateRequest request = new FollowCreateRequest(1L);

            // when, then
            assertThatThrownBy(() -> followService.createFollow(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 본인을_팔로우_할_경우_예외를_발생시킨다() {
            FollowCreateRequest request = new FollowCreateRequest(1L);
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname1", "testImageUrl1")));

            // when, then
            assertThatThrownBy(() -> followService.createFollow(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.FOLLOW_SELF_NOT_ALLOWED.getMessage());
        }

        @Test
        void 타겟회원이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            Long targetId = 2L;
            FollowCreateRequest request = new FollowCreateRequest(targetId);
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname1", "testImageUrl1")));

            // when, then
            assertThatThrownBy(() -> followService.createFollow(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.FOLLOW_TARGET_MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_팔로우를_하고있다면_예외를_발생시킨다() {
            Long targetId = 2L;
            FollowCreateRequest request = new FollowCreateRequest(targetId);
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname1", "testImageUrl1")));
            Member targetMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname2", "testImageUrl2")));
            MemberRelation memberRelation =
                    MemberRelation.createMemberRelation(currentMember, targetMember);
            memberRelationRepository.save(memberRelation);

            // when, then
            assertThatThrownBy(() -> followService.createFollow(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.FOLLOW_ALREADY_EXIST.getMessage());
        }

        // @Test
        // void 정상적이라면_팔로우가_추가된다() {
        //     Long targetId = 2L;
        //     FollowCreateRequest request = new FollowCreateRequest(targetId);
        //     Member currentMember =
        //             memberRepository.save(
        //                     Member.createNormalMember(
        //                             Profile.createProfile("testNickname1", "testImageUrl1")));
        //     Member targetMember =
        //             memberRepository.save(
        //                     Member.createNormalMember(
        //                             Profile.createProfile("testNickname2", "testImageUrl2")));
        //
        //     // when
        //     followService.createFollow(request);
        //
        //     // then
        //     assertEquals(1, memberRelationRepository.count());
        //     assertEquals(
        //             currentMember.getId(),
        //             memberRelationRepository.findAll().get(0).getSource().getId());
        //     assertEquals(
        //             targetMember.getId(),
        //             memberRelationRepository.findAll().get(0).getTarget().getId());
        // }
    }

    @Nested
    class 팔로우를_취소할_때 {
        @Test
        void 로그인된_회원이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            FollowDeleteRequest request = new FollowDeleteRequest(1L);

            // when, then
            assertThatThrownBy(() -> followService.deleteFollow(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 타겟회원이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            Long targetId = 2L;
            FollowDeleteRequest request = new FollowDeleteRequest(targetId);
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname1", "testImageUrl1")));

            // when, then
            assertThatThrownBy(() -> followService.deleteFollow(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.FOLLOW_TARGET_MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 팔로우를_하고있지_않다면_예외를_발생시킨다() {
            Long targetId = 2L;
            FollowDeleteRequest request = new FollowDeleteRequest(targetId);
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname1", "testImageUrl1")));
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname2", "testImageUrl2")));

            // when, then
            assertThatThrownBy(() -> followService.deleteFollow(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.FOLLOW_NOT_EXIST.getMessage());
        }

        @Test
        void 상대가_나를_팔로우_하고_있다면_FOLLOW_STATUE가_FOLLOWED_BY_ME로_응답한다() {
            Long targetId = 2L;
            FollowDeleteRequest request = new FollowDeleteRequest(targetId);
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname1", "testImageUrl1")));
            Member targetMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname2", "testImageUrl2")));
            MemberRelation memberRelation =
                    MemberRelation.createMemberRelation(currentMember, targetMember);
            MemberRelation memberRelation2 =
                    MemberRelation.createMemberRelation(targetMember, currentMember);
            memberRelationRepository.save(memberRelation);
            memberRelationRepository.save(memberRelation2);

            // when
            FollowerDeletedResponse response = followService.deleteFollow(request);

            // then
            assertEquals(FollowStatus.FOLLOWED_BY_ME, response.followStatus());
        }

        @Test
        void 상대가_나를_팔로우_하고_있지_않다면_FOLLOW_STATUE가_NOT_FOLLOWING로_응답한다() {
            Long targetId = 2L;
            FollowDeleteRequest request = new FollowDeleteRequest(targetId);
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname1", "testImageUrl1")));
            Member targetMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname2", "testImageUrl2")));
            MemberRelation memberRelation =
                    MemberRelation.createMemberRelation(currentMember, targetMember);
            memberRelationRepository.save(memberRelation);

            // when
            FollowerDeletedResponse response = followService.deleteFollow(request);

            // then
            assertEquals(FollowStatus.NOT_FOLLOWING, response.followStatus());
        }

        @Test
        void 정상적이라면_팔로우가_취소된다() {
            Long targetId = 2L;
            FollowDeleteRequest request = new FollowDeleteRequest(targetId);
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname1", "testImageUrl1")));
            Member targetMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname2", "testImageUrl2")));
            MemberRelation memberRelation =
                    MemberRelation.createMemberRelation(currentMember, targetMember);
            memberRelationRepository.save(memberRelation);

            // when
            followService.deleteFollow(request);

            // then
            assertEquals(0, memberRelationRepository.count());
        }
    }

    @Nested
    class 타인의_팔로우_카운트를_확인할_때 {
        @Test
        void 로그인된_회원이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            Long targetId = 2L;

            // when, then
            assertThatThrownBy(() -> followService.findTargetFollowInfo(targetId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 타겟회원이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            Long targetId = 2L;
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname1", "testImageUrl1")));

            // when, then
            assertThatThrownBy(() -> followService.findTargetFollowInfo(targetId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.FOLLOW_TARGET_MEMBER_NOT_FOUND.getMessage());
        }

        @Nested
        class 타겟유저가_나를_팔로우하고_있고 {
            @Test
            void 나도_팔로우하고있는_경우에_FollowStatus가_FOLLOWING로_반환된다() {
                // given
                Member currentMember =
                        memberRepository.save(
                                Member.createNormalMember(
                                        Profile.createProfile("testNickname1", "testImageUrl1")));
                Member targetMember =
                        memberRepository.save(
                                Member.createNormalMember(
                                        Profile.createProfile("testNickname2", "testImageUrl2")));
                memberRelationRepository.save(
                        MemberRelation.createMemberRelation(targetMember, currentMember));
                memberRelationRepository.save(
                        MemberRelation.createMemberRelation(currentMember, targetMember));

                // when
                FollowFindTargetInfoResponse response =
                        followService.findTargetFollowInfo(targetMember.getId());

                // then
                assertEquals(FollowStatus.FOLLOWING, response.followStatus());
            }

            @Test
            void 나는_타겟유저를_팔로우하지_않는_경우에_FollowStatus가_FOLLOWED_BY_ME로_반환된다() {
                // given
                Member currentMember =
                        memberRepository.save(
                                Member.createNormalMember(
                                        Profile.createProfile("testNickname1", "testImageUrl1")));
                Member targetMember =
                        memberRepository.save(
                                Member.createNormalMember(
                                        Profile.createProfile("testNickname2", "testImageUrl2")));
                memberRelationRepository.save(
                        MemberRelation.createMemberRelation(targetMember, currentMember));

                // when
                FollowFindTargetInfoResponse response =
                        followService.findTargetFollowInfo(targetMember.getId());

                // then
                assertEquals(FollowStatus.FOLLOWED_BY_ME, response.followStatus());
            }
        }

        @Nested
        class 타겟유저가_나를_팔로우_하지_않고 {
            @Test
            void 나는_팔로우하고있는_경우에_FollowStatus가_FOLLOWING로_반환된다() {
                // given
                Member currentMember =
                        memberRepository.save(
                                Member.createNormalMember(
                                        Profile.createProfile("testNickname1", "testImageUrl1")));
                Member targetMember =
                        memberRepository.save(
                                Member.createNormalMember(
                                        Profile.createProfile("testNickname2", "testImageUrl2")));
                memberRelationRepository.save(
                        MemberRelation.createMemberRelation(currentMember, targetMember));

                // when
                FollowFindTargetInfoResponse response =
                        followService.findTargetFollowInfo(targetMember.getId());

                // then
                assertEquals(FollowStatus.FOLLOWING, response.followStatus());
            }

            @Test
            void 나도_팔로우하지_않는_경우에_FollowStatus가_NOT_FOLLOWING로_반환된다() {
                // given
                Member currentMember =
                        memberRepository.save(
                                Member.createNormalMember(
                                        Profile.createProfile("testNickname1", "testImageUrl1")));
                Member targetMember =
                        memberRepository.save(
                                Member.createNormalMember(
                                        Profile.createProfile("testNickname2", "testImageUrl2")));

                // when
                FollowFindTargetInfoResponse response =
                        followService.findTargetFollowInfo(targetMember.getId());

                // then
                assertEquals(FollowStatus.NOT_FOLLOWING, response.followStatus());
            }
        }

        @Test
        void 정상적이라면_타겟유저의_정보가_정상적으로_조회된다() {
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname1", "testImageUrl1")));
            Member targetMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname2", "testImageUrl2")));
            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(targetMember, currentMember));

            // when
            FollowFindTargetInfoResponse response =
                    followService.findTargetFollowInfo(targetMember.getId());

            // then
            assertEquals(FollowStatus.FOLLOWED_BY_ME, response.followStatus());
            assertEquals(1L, response.followingCount());
            assertEquals(0L, response.followerCount());
        }
    }

    @Nested
    class 나의_팔로우_카운트를_확인할_때 {
        @Test
        void 로그인된_회원이_존재하지_않는다면_예외를_발생시킨다() {
            // when, then
            assertThatThrownBy(() -> followService.findMeFollowInfo())
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 정상적이라면_나의_정보가_정상적으로_조회된다() {
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname1", "testImageUrl1")));
            Member targetMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("testNickname2", "testImageUrl2")));
            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(targetMember, currentMember));

            // when
            FollowFindMeInfoResponse response = followService.findMeFollowInfo();

            // then
            assertEquals(0L, response.followingCount());
            assertEquals(1L, response.followerCount());
        }
    }

    @Nested
    class 내가_팔로우한_유저_정보_리스트를_조회할_때 {
        @Test
        void 로그인된_회원이_존재하지_않는다면_예외를_발생시킨다() {
            // when, then
            assertThatThrownBy(() -> followService.findAllFollowedMember())
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 내가_팔로우한_사람이_없다면_빈_리스트가_조회된다() {
            // given
            memberRepository.save(
                    Member.createNormalMember(
                            Profile.createProfile("testNickname1", "testImageUrl1")));

            // when
            List<MemberFollowedResponse> response = followService.findAllFollowedMember();

            // then
            assertEquals(0, response.size());
        }

        @Test
        void 팔로우한_유저가_당일_미션을_완수하였다면_미션을_완수하지_않은_유저보다_먼저_조회된다() {
            // given
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("currentMember", "currentMember")));
            Member targetMember1 =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("targetMember1", "targetMember1")));
            Member targetMember2 =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("targetMember2", "targetMember2")));

            LocalDateTime today = LocalDateTime.now();
            LocalDateTime missionStartedAt = today;
            LocalDateTime missionFinishedAt = today.plusWeeks(2);
            Mission mission =
                    missionRepository.save(
                            Mission.createMission(
                                    "testMissionName",
                                    "testMissionContent",
                                    1,
                                    MissionCategory.ETC,
                                    MissionVisibility.ALL,
                                    missionStartedAt,
                                    missionFinishedAt,
                                    null,
                                    targetMember2));

            LocalDateTime missionRecordStartedAt = today;
            LocalDateTime missionRecordFinishedAt =
                    missionRecordStartedAt.plusMinutes(32).plusSeconds(14);
            Duration duration = Duration.ofMinutes(32).plusSeconds(14);

            MissionRecord missionRecord =
                    missionRecordRepository.save(
                            MissionRecord.createMissionRecord(
                                    duration,
                                    missionRecordStartedAt,
                                    missionRecordFinishedAt,
                                    mission));
            missionRecord.updateUploadStatusPending();
            missionRecord.updateUploadStatusComplete("remark", "imageUrl");
            missionRecordRepository.save(missionRecord);

            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(currentMember, targetMember1));
            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(currentMember, targetMember2));

            // when
            List<MemberFollowedResponse> response = followService.findAllFollowedMember();

            // then
            assertEquals(2, response.size());
            assertEquals("targetMember2", response.get(0).nickname());
            assertEquals("targetMember1", response.get(1).nickname());
        }

        @Test
        void 팔로우한_유저가_당일_미션을_완수하지_않았다면_팔로우_시간_기준으로_조회된다() {
            // given
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("currentMember", "currentMember")));
            Member targetMember1 =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("targetMember1", "targetMember1")));
            Member targetMember2 =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("targetMember2", "targetMember2")));

            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(currentMember, targetMember1));
            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(currentMember, targetMember2));

            // when
            List<MemberFollowedResponse> response = followService.findAllFollowedMember();

            // then
            assertEquals(2, response.size());
            assertEquals("targetMember1", response.get(0).nickname());
            assertEquals("targetMember2", response.get(1).nickname());
        }
    }

    @Nested
    class 나의_팔로워를_삭제할_때 {
        @Test
        void 로그인된_회원이_존재하지_않는다면_예외를_발생시킨다() {
            // when, then
            assertThatThrownBy(() -> followService.deleteFollower(224L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 삭제하려는_회원이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("currentMember", "currentMember")));

            // when, then
            assertThatThrownBy(() -> followService.deleteFollower(224L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.FOLLOW_TARGET_MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 삭제하려는_유저가_나의_팔로워가_아니라면_예외가_발생한다() {
            // given
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("currentMember", "currentMember")));
            Member targetMember1 =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("targetMember1", "targetMember1")));
            // when, then
            assertThatThrownBy(() -> followService.deleteFollower(targetMember1.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.FOLLOW_NOT_EXIST.getMessage());
        }

        @Test
        void 내가_팔로우_하고_있다면_FOLLOWER_STATUS가_FOLLOWING로_응답한다() {
            // given
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("currentMember", "currentMember")));
            Member targetMember1 =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("targetMember1", "targetMember1")));
            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(targetMember1, currentMember));
            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(currentMember, targetMember1));

            // when
            FollowerDeletedResponse response = followService.deleteFollower(targetMember1.getId());

            // then
            assertEquals(FollowStatus.FOLLOWING, response.followStatus());
        }

        @Test
        void 내가_팔로우_하고_있지_않다면_FOLLOWER_STATUS가_NOT_FOLLOWING로_응답한다() {
            // given
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("currentMember", "currentMember")));
            Member targetMember1 =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("targetMember1", "targetMember1")));
            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(targetMember1, currentMember));

            // when
            FollowerDeletedResponse response = followService.deleteFollower(targetMember1.getId());

            // then
            assertEquals(FollowStatus.NOT_FOLLOWING, response.followStatus());
        }
    }
}
