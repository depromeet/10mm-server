package com.depromeet.domain.member.application;

import static com.depromeet.global.common.constants.PushNotificationConstants.*;

import com.depromeet.domain.auth.dao.RefreshTokenRepository;
import com.depromeet.domain.auth.dto.request.UsernameCheckRequest;
import com.depromeet.domain.follow.dao.MemberRelationRepository;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.follow.dto.response.MemberRelationPair;
import com.depromeet.domain.image.domain.ImageFileExtension;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.MemberStatus;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.domain.member.dto.request.NicknameUpdateRequest;
import com.depromeet.domain.member.dto.request.UpdateFcmTokenRequest;
import com.depromeet.domain.member.dto.response.MemberFindOneResponse;
import com.depromeet.domain.member.dto.response.MemberSearchResponse;
import com.depromeet.domain.member.dto.response.MemberSocialInfoResponse;
import com.depromeet.domain.notification.application.FcmService;
import com.depromeet.global.common.response.CursorSliceResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.CursorCodec;
import com.depromeet.global.util.MemberUtil;
import com.depromeet.global.util.SecurityUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
    private final CursorCodec cursorCodec;
    private final SecurityUtil securityUtil;

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
                memberRelationRepository.findAllByTargetIdAndSourceIn(
                        currentMember.getId(), members);
        Set<Long> followingIds =
                memberRelationBySourceId.stream()
                        .map(relation -> relation.getTarget().getId())
                        .collect(Collectors.toSet());
        Set<Long> followerIds =
                memberRelationByTargetId.stream()
                        .map(relation -> relation.getSource().getId())
                        .collect(Collectors.toSet());

        return members.stream()
                .map(member -> MemberSearchResponse.of(member, followingIds, followerIds))
                .sorted(
                        Comparator.comparing(
                                        (MemberSearchResponse response) ->
                                                response.nickname().equals(nickname) ? 0 : 1)
                                .thenComparing(MemberSearchResponse::nickname))
                .toList();
    }

    @Transactional(readOnly = true)
    public CursorSliceResponse<MemberSearchResponse> searchMemberNicknameV2(
            String nickname, int size, String cursor) {
        final Long currentMemberId = securityUtil.getCurrentMemberId();
        final String escapingNickname = escapeSpecialCharacters(nickname);
        if (escapingNickname.isBlank()) {
            return CursorSliceResponse.of(List.of(), null, false);
        }

        Integer cursorPriority = null;
        String cursorNickname = null;
        Long cursorMemberId = null;
        if (cursor != null && !cursor.isBlank()) {
            List<String> values = cursorCodec.decode(cursor, 3);
            cursorPriority = cursorCodec.integerValue(values, 0);
            cursorNickname = values.get(1);
            cursorMemberId = cursorCodec.longValue(values, 2);
        }

        List<Member> members =
                memberRepository.nicknameSearchPage(
                        escapingNickname,
                        currentMemberId,
                        cursorPriority,
                        cursorNickname,
                        cursorMemberId,
                        size + 1);
        boolean hasNext = members.size() > size;
        if (hasNext) {
            members = new ArrayList<>(members.subList(0, size));
        }

        List<Long> memberIds = members.stream().map(Member::getId).toList();
        List<MemberRelationPair> relations =
                memberRelationRepository.findRelationsBetween(currentMemberId, memberIds);
        Set<Long> followingIds = MemberRelationPair.followingIds(relations, currentMemberId);
        Set<Long> followerIds = MemberRelationPair.followerIds(relations, currentMemberId);
        List<MemberSearchResponse> content =
                members.stream()
                        .map(member -> MemberSearchResponse.of(member, followingIds, followerIds))
                        .toList();

        String nextCursor = null;
        if (hasNext && !members.isEmpty()) {
            Member lastMember = members.getLast();
            int exactMatchPriority =
                    lastMember.getProfile().getNickname().equals(escapingNickname) ? 0 : 1;
            nextCursor =
                    cursorCodec.encode(
                            exactMatchPriority,
                            lastMember.getProfile().getNickname(),
                            lastMember.getId());
        }
        return CursorSliceResponse.of(content, nextCursor, hasNext);
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
        List<Member> nonMissionNonCompletedMembers =
                memberRepository.findMissionNonCompletedMembers(today);
        List<String> tokenList =
                nonMissionNonCompletedMembers.stream()
                        .map(member -> member.getFcmInfo().getFcmToken())
                        .toList();
        if (!tokenList.isEmpty()) {
            fcmService.sendGroupMessageAsync(
                    tokenList, PUSH_SERVICE_TITLE, PUSH_NON_COMPLETE_MISSION_SERVICE_CONTENT);
        }
    }

    private String escapeSpecialCharacters(String nickname) {
        // 여기서 특수문자를 '_'로 대체할 수 있도록 정규표현식을 활용하여 구현
        return nickname == null ? "" : nickname.replaceAll("[^0-9a-zA-Z가-힣 ]", "_");
    }

    @Transactional(readOnly = true)
    public List<Member> findAllNormalMember() {
        return memberRepository.findAllByStatusIs(MemberStatus.NORMAL);
    }
}
