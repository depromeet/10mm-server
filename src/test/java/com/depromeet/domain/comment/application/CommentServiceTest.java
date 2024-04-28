package com.depromeet.domain.comment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.depromeet.domain.comment.dao.CommentRepository;
import com.depromeet.domain.comment.dto.request.CommentCreateRequest;
import com.depromeet.domain.comment.dto.request.CommentUpdateRequest;
import com.depromeet.domain.comment.dto.response.CommentDto;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
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
class CommentServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 3, 26, 5, 0, 0);

    @Autowired private CommentService commentService;
    @Autowired private CommentRepository commentRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MissionRepository missionRepository;
    @Autowired private MissionRecordRepository missionRecordRepository;

    private Member saveAndRegisterMember() {
        SecurityContextHolder.clearContext();
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
                        null,
                        member);
        missionRepository.save(mission);
        MissionRecord missionRecord =
                MissionRecord.createMissionRecord(
                        Duration.ofMinutes(30), NOW.minusMinutes(25), NOW.minusMinutes(5), mission);
        missionRecordRepository.save(missionRecord);
    }

    private void logoutAndReloginAs(Long memberId) {
        SecurityContextHolder.clearContext(); // 현재 회원 로그아웃
        PrincipalDetails principalDetails = new PrincipalDetails(memberId, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    class 댓글_생성시 {

        @Test
        void 성공한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);

            // when
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            CommentDto commentDto = commentService.createComment(request);

            // then
            assertThat(commentDto).isNotNull();
            assertThat(commentDto.commentId()).isEqualTo(1L);
            assertThat(commentDto.content()).isEqualTo("testContent");
        }

        @Test
        void 존재하지_않는_미션기록이면_실패한다() {
            // given
            Member member = saveAndRegisterMember();

            // when & then
            CommentCreateRequest request = new CommentCreateRequest(2L, "testContent");

            assertThatThrownBy(() -> commentService.createComment(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MISSION_RECORD_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 댓글_수정시 {

        @Test
        void 성공한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            commentService.createComment(request);

            // when
            CommentUpdateRequest updateRequest = new CommentUpdateRequest("updatedContent");
            CommentDto updatedCommentDto = commentService.updateComment(1L, updateRequest);

            // then
            assertThat(updatedCommentDto).isNotNull();
            assertThat(updatedCommentDto.commentId()).isEqualTo(1L);
            assertThat(updatedCommentDto.content()).isEqualTo("updatedContent");
        }

        @Test
        void 존재하지_않는_댓글이면_실패한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);

            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            commentService.createComment(request);

            // when & then
            CommentUpdateRequest updateRequest = new CommentUpdateRequest("updatedContent");

            assertThatThrownBy(() -> commentService.updateComment(2L, updateRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());
        }

        @Test
        void 다른_사용자가_작성한_댓글이면_실패한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            commentService.createComment(request);

            // when & then
            saveAndRegisterMember(); // 다른 사용자로 로그인
            CommentUpdateRequest updateRequest = new CommentUpdateRequest("updatedContent");

            assertThatThrownBy(() -> commentService.updateComment(1L, updateRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.COMMENT_MEMBER_MISMATCH.getMessage());
        }
    }

    @Nested
    class 댓글_삭제시 {

        @Test
        void 성공한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            commentService.createComment(request);

            // when
            commentService.deleteComment(1L);

            // then
            assertThat(commentRepository.findById(1L)).isEmpty();
        }

        @Test
        void 존재하지_않는_댓글이면_실패한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            commentService.createComment(request);

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(2L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());
        }

        @Test
        void 다른_사용자가_작성한_댓글이면_실패한다() {
            // given
            Member member = saveAndRegisterMember();
            createMissionAndMissionRecord(member);
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            commentService.createComment(request);

            // when & then
            saveAndRegisterMember(); // 다른 사용자로 로그인

            assertThatThrownBy(() -> commentService.deleteComment(1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.COMMENT_MEMBER_MISMATCH.getMessage());
        }
    }
}
