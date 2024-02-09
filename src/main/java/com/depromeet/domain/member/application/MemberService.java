package com.depromeet.domain.member.application;

import com.depromeet.domain.auth.dao.RefreshTokenRepository;
import com.depromeet.domain.auth.dto.request.UsernameCheckRequest;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.image.domain.ImageFileExtension;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.domain.member.dto.request.NicknameUpdateRequest;
import com.depromeet.domain.member.dto.request.UpdateFcmTokenRequest;
import com.depromeet.domain.member.dto.response.MemberFindOneResponse;
import com.depromeet.domain.member.dto.response.MemberSearchResponse;
import com.depromeet.domain.member.dto.response.MemberSocialInfoResponse;
import com.depromeet.global.config.fcm.FcmService;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRelationRepository memberRelationRepository;
    private final MemberUtil memberUtil;
    private final FcmService fcmService;

    private static final String NON_COMPLETE_MISSION_TITLE = "10MM";
    private static final String NON_COMPLETE_MISSION_CONTENT =
            "아직 오늘 미션을 완료하지 않았어요! 10분 동안 빠르게 완료해볼까요?";

    @Transactional(readOnly = true)
    public MemberFindOneResponse findMemberInfo() {
        final Member currentMember = memberUtil.getCurrentMember();
        ImageFileExtension imageFileExtension = getImageFileExtension(currentMember.getProfile());
        return MemberFindOneResponse.of(currentMember, imageFileExtension);
    }

    @Transactional(readOnly = true)
    public MemberFindOneResponse findTargetInfo(Long targetId) {
        final Member targetMember = memberUtil.getMemberByMemberId(targetId);
        ImageFileExtension imageFileExtension = getImageFileExtension(targetMember.getProfile());
        return MemberFindOneResponse.of(targetMember, imageFileExtension);
    }

    @Transactional(readOnly = true)
    public void checkUsername(UsernameCheckRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_REGISTERED);
        }
    }

    @Transactional(readOnly = true)
    public void checkNickname(NicknameCheckRequest request) {
        validateNicknameNotDuplicate(request.nickname());
        if (validateNicknameText(request.nickname())) {
            throw new CustomException(ErrorCode.MEMBER_INVALID_NICKNAME);
        }
    }

    private boolean validateNicknameText(String nickname) {
        return nickname == null || nickname.trim().isEmpty();
    }

    private void validateNicknameNotDuplicate(String nickname) {
        if (memberRepository.existsByProfileNickname(nickname)) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_NICKNAME);
        }
    }

    @Transactional(readOnly = true)
    public List<MemberSearchResponse> searchMemberNickname(String nickname) {
        final Member currentMember = memberUtil.getCurrentMember();
        final String escapingNickname = escapeSpecialCharacters(nickname);
        if (escapingNickname.isBlank()) {
            return List.of();
        }

        List<Member> members =
                memberRepository.nicknameSearch(
                        escapingNickname, currentMember.getProfile().getNickname());
        List<MemberRelation> memberRelationBySourceId =
                memberRelationRepository.findAllBySourceIdAndTargetIn(
                        currentMember.getId(), members);
        List<MemberRelation> memberRelationByTargetId =
                memberRelationRepository.findAllByTargetId(currentMember.getId());

        List<MemberSearchResponse> response = new ArrayList<>();
        for (Member member : members) {
            boolean existRelation = false;
            for (MemberRelation memberRelation : memberRelationBySourceId) {
                if (member.getId().equals(memberRelation.getTarget().getId())) {
                    existRelation = true;
                    break;
                }
            }

            if (existRelation) { // 닉네임 검색한 애들 중 내가 팔로우한 애라면
                response.add(MemberSearchResponse.toFollowingResponse(member));
                continue;
            }

            // 내가 팔로우를 하지 않았을 때
            Optional<MemberRelation> optionalMemberRelation =
                    memberRelationByTargetId.stream()
                            .filter(
                                    memberRelation ->
                                            member.getId()
                                                    .equals(memberRelation.getSource().getId()))
                            .findFirst();
            if (optionalMemberRelation.isPresent()) { // 상대방만 나를 팔로우 하고 있을  때
                response.add(MemberSearchResponse.toFollowedByMeResponse(member));
                continue;
            }

            // 아니라면 서로 팔로우가 아닌 상태
            response.add(MemberSearchResponse.toNotFollowingResponse(member));
        }
        response =
                response.stream()
                        .sorted(Comparator.comparing(MemberSearchResponse::nickname))
                        .sorted(
                                Comparator.comparing(
                                        MemberSearchResponse ->
                                                MemberSearchResponse.nickname().equals(nickname)
                                                        ? 0
                                                        : 1))
                        .collect(Collectors.toList());
        return response;
    }

    public void withdrawal() {
        final Member member = memberUtil.getCurrentMember();
        refreshTokenRepository.deleteById(member.getId());
        member.withdrawal();
    }

    public MemberSocialInfoResponse findMemberSocialInfo() {
        final Member currentMember = memberUtil.getCurrentMember();
        validateSocialInfoNotNull(currentMember);
        return MemberSocialInfoResponse.from(currentMember);
    }

    private void validateSocialInfoNotNull(Member member) {
        if (member.getOauthInfo() == null) {
            throw new CustomException(ErrorCode.MEMBER_SOCIAL_INFO_NOT_FOUND);
        }
    }

    public void updateMemberNickname(NicknameUpdateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        validateNicknameNotDuplicate(request.nickname());
        currentMember.updateNickname(escapeSpecialCharacters(request.nickname()));
    }

    private ImageFileExtension getImageFileExtension(Profile profile) {
        // TODO: 이미지 확장자 정보 같이 넘겨주는 작업 추가 (24.01.26)
        // 이미지 업로드와 닉네임 변경 분리 후 제거 예정
        ImageFileExtension imageFileExtension = null;
        if (profile.getProfileImageUrl() != null) {
            String profileImageUrl = profile.getProfileImageUrl();
            String extension = profileImageUrl.substring(profileImageUrl.lastIndexOf(".") + 1);
            imageFileExtension = ImageFileExtension.of(extension);
        }
        return imageFileExtension;
    }

    public void toggleAppAlarm() {
        final Member currentMember = memberUtil.getCurrentMember();
        currentMember.toggleAppAlarmState(currentMember.getFcmInfo());
    }

    public void updateFcmToken(UpdateFcmTokenRequest updateFcmTokenRequest) {
        final Member currentMember = memberUtil.getCurrentMember();
        currentMember.updateFcmToken(currentMember.getFcmInfo(), updateFcmTokenRequest.fcmToken());
    }

    @Transactional(readOnly = true)
    public void pushNotificationMissionRequest() {
        LocalDateTime today = LocalDateTime.now();
        List<Member> nonCompleteMissions = memberRepository.findNonCompleteMissions(today);
        List<String> tokenList =
                nonCompleteMissions.stream()
                        .map(member -> member.getFcmInfo().getFcmToken())
                        .toList();
        if (!tokenList.isEmpty()) {
            fcmService.sendGroupMessageAsync(
                    tokenList, NON_COMPLETE_MISSION_TITLE, NON_COMPLETE_MISSION_CONTENT);
        }
    }

    private String escapeSpecialCharacters(String nickname) {
        // 여기서 특수문자를 '_'로 대체할 수 있도록 정규표현식을 활용하여 구현
        return nickname == null ? "" : nickname.replaceAll("[^0-9a-zA-Z가-힣 ]", "_");
    }
}
