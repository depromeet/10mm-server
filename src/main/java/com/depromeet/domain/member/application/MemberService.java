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
import com.depromeet.domain.member.dto.response.MemberFindOneResponse;
import com.depromeet.domain.member.dto.response.MemberSearchResponse;
import com.depromeet.domain.member.dto.response.MemberSocialInfoResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
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
        if (memberRepository.existsByProfileNickname(request.nickname())) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_NICKNAME);
        }
    }

    @Transactional(readOnly = true)
    public List<MemberSearchResponse> searchMemberNickname(String nickname) {
        final Member currentMember = memberUtil.getCurrentMember();
        List<Member> members =
                memberRepository.nicknameSearch(nickname, currentMember.getProfile().getNickname());
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

            if (existRelation) {
                Optional<MemberRelation> optionalMemberRelation =
                        memberRelationByTargetId.stream()
                                .filter(
                                        memberRelation ->
                                                member.getId()
                                                        .equals(memberRelation.getSource().getId()))
                                .findFirst();
                if (optionalMemberRelation.isPresent()) {
                    response.add(MemberSearchResponse.toFollowedByMeResponse(member));
                    continue;
                }

                response.add(MemberSearchResponse.toFollowingResponse(member));
                continue;
            }
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

    public void withdrawal(UsernameCheckRequest request) {
        final Member member =
                memberRepository
                        .findByUsername(request.username())
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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
}
