package com.depromeet.domain.member.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.auth.domain.OauthProvider;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.follow.dto.response.FollowStatus;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.member.dto.response.MemberSearchResponse;
import com.depromeet.domain.member.dto.response.MemberSocialInfoResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.security.PrincipalDetails;
import com.depromeet.global.util.MemberUtil;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser
class MemberServiceTest {

    @Autowired EntityManager em;
    @Autowired DatabaseCleaner databaseCleaner;
    @Autowired MemberUtil memberUtil;
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberRelationRepository memberRelationRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    private void saveAndRegisterMember(OauthInfo oauthInfo) {
        Member member = Member.createGuestMember(oauthInfo, "testNickname");
        memberRepository.save(member);
        member.register("testNickname");
        PrincipalDetails principalDetails = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    class 소셜_로그인_정보를_조회할때 {
        @Test
        void 성공한다() {
            // given
            OauthInfo oauthInfo =
                    OauthInfo.createOauthInfo(
                            "testOauthId", OauthProvider.KAKAO.getIssuer(), "testEmail");
            saveAndRegisterMember(oauthInfo);

            // when
            MemberSocialInfoResponse response = memberService.findMemberSocialInfo();

            // then
            assertEquals(OauthProvider.KAKAO, response.provider());
            assertEquals("testEmail", response.email());
        }

        @Test
        void 멤버의_프로바이더_정보와_일치하는_issuer가_없으면_예외가_발생한다() {
            // given
            String invalidIssuer = "invalidIssuer";
            OauthInfo oauthInfo =
                    OauthInfo.createOauthInfo("testOauthId", invalidIssuer, "testEmail");
            saveAndRegisterMember(oauthInfo);

            // when & then
            assertThrows(
                    CustomException.class,
                    () -> memberService.findMemberSocialInfo(),
                    ErrorCode.OAUTH_PROVIDER_NOT_FOUND.getMessage());
        }

        @Test
        void 멤버의_프로바이더가_null이면_예외가_발생한다() {
            // given
            OauthInfo oauthInfo = OauthInfo.createOauthInfo("testOauthId", null, "testEmail");
            saveAndRegisterMember(oauthInfo);

            // when & then
            assertThrows(
                    CustomException.class,
                    () -> memberService.findMemberSocialInfo(),
                    ErrorCode.OAUTH_PROVIDER_NOT_FOUND.getMessage());
        }

        @Test
        void 멤버의_OauthInfo가_비어있으면_예외가_발생한다() {
            // given
            saveAndRegisterMember(null);

            // when & then
            assertThrows(
                    CustomException.class,
                    () -> memberService.findMemberSocialInfo(),
                    ErrorCode.MEMBER_SOCIAL_INFO_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 닉네임으로_회원을_검색할_때 {

        @BeforeEach
        void setUp() {
            databaseCleaner.execute();
            PrincipalDetails principal = new PrincipalDetails(1L, "USER");
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal, "password", principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        void 로그인된_회원이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            String searchNickname = "도모";

            // when, then
            assertThatThrownBy(() -> memberService.searchMemberNickname(searchNickname))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        void 검색_키워드에_해당하는_닉네임이_없다면_빈_리스트가_조회된다() {
            // given
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(Profile.createProfile("도모", "testImageUrl")));

            String searchNickname = "잘생긴";

            // when
            List<MemberSearchResponse> responses =
                    memberService.searchMemberNickname(searchNickname);

            // then
            assertEquals(0, responses.size());
        }

        @Test
        void 검색키워드에_본인이_해당되어도_본인은_검색되지_않아야한다() {
            // given
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(Profile.createProfile("도모", "도모 이미지 URL")));
            Member searchMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("도모 바보", "testImageUrl")));

            String searchNickname = "도모";

            // when
            List<MemberSearchResponse> responses =
                    memberService.searchMemberNickname(searchNickname);

            // then
            assertEquals(1, responses.size());
            assertFalse(responses.contains(currentMember.getProfile().getNickname()));
        }

        @Test
        void 정렬조건은_일치하는경우_먼저보여주고_나머지는_사전순에_따른다() {
            // given
            String searchNickname = "도모";
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("currentMember", "도모 이미지 URL")));

            Member searchMember1 =
                    memberRepository.save(
                            Member.createNormalMember(Profile.createProfile("도모1", "윤범 이미지 URL")));
            Member searchMember2 =
                    memberRepository.save(
                            Member.createNormalMember(Profile.createProfile("ㄱ도모1", "윤범 이미지 URL")));
            Member searchMember3 =
                    memberRepository.save(
                            Member.createNormalMember(Profile.createProfile("도모", "도모 이미지 URL")));

            // when
            List<MemberSearchResponse> responses =
                    memberService.searchMemberNickname(searchNickname);

            // then
            assertEquals(3, responses.size());
            assertEquals(searchMember3.getProfile().getNickname(), responses.get(0).nickname());
            assertEquals(searchMember2.getProfile().getNickname(), responses.get(1).nickname());
            assertEquals(searchMember1.getProfile().getNickname(), responses.get(2).nickname());
        }

        @Test
        void 검색된_회원의_팔로우_상태를_확인할_수_있다() {
            // given
            String searchNickname = "잘생긴";
            Member currentMember =
                    memberRepository.save(
                            Member.createNormalMember(Profile.createProfile("도모", "도모 이미지 URL")));
            Member searchMember1 =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("잘생긴 윤범", "윤범 이미지 URL")));
            Member searchMember2 =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("잘생긴 재현", "재현 이미지 URL")));
            Member searchMember3 =
                    memberRepository.save(
                            Member.createNormalMember(
                                    Profile.createProfile("잘생긴 우병", "우병 이미지 URL")));

            // 도모가 윤범이만 팔로우
            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(currentMember, searchMember1));

            // 도모와 재현이는 맞팔로우
            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(currentMember, searchMember2));
            memberRelationRepository.save(
                    MemberRelation.createMemberRelation(searchMember2, currentMember));

            // when
            List<MemberSearchResponse> responses =
                    memberService.searchMemberNickname(searchNickname);

            // then
            assertEquals(3, responses.size());

            // 도모와 우병은 팔로우관계가 아니다.
            assertEquals(FollowStatus.NOT_FOLLOWING, responses.get(0).followStatus());

            // 도모만 윤범이를 팔로우하고있다.
            assertEquals(FollowStatus.FOLLOWING, responses.get(1).followStatus());

            // 도모는 재현이와 맞팔로우 관계이다.
            assertEquals(FollowStatus.FOLLOWED_BY_ME, responses.get(2).followStatus());
        }
    }
}
