package com.depromeet.domain.follow.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.follow.dto.request.FollowCreateRequest;
import com.depromeet.domain.follow.dto.request.FollowDeleteRequest;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.security.PrincipalDetails;
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
class FollowServiceTest {
    @Autowired private DatabaseCleaner databaseCleaner;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberRelationRepository memberRelationRepository;
    @Autowired private FollowService followService;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
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

        @Test
        void 정상적이라면_팔로우가_추가된다() {
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

            // when
            followService.createFollow(request);

            // then
            assertEquals(1, memberRelationRepository.count());
            assertEquals(
                    currentMember.getId(),
                    memberRelationRepository.findAll().get(0).getSource().getId());
            assertEquals(
                    targetMember.getId(),
                    memberRelationRepository.findAll().get(0).getTarget().getId());
        }
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
}
