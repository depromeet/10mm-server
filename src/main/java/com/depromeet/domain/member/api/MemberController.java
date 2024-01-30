package com.depromeet.domain.member.api;

import com.depromeet.domain.auth.dto.request.UsernameCheckRequest;
import com.depromeet.domain.member.application.MemberService;
import com.depromeet.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.domain.member.dto.request.NicknameUpdateRequest;
import com.depromeet.domain.member.dto.response.MemberFindOneResponse;
import com.depromeet.domain.member.dto.response.MemberSearchResponse;
import com.depromeet.domain.member.dto.response.MemberSocialInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "1-2. [회원]", description = "회원 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 정보 확인", description = "로그인 된 회원의 정보를 확인합니다.")
    @GetMapping("/me")
    public MemberFindOneResponse memberInfo() {
        return memberService.findMemberInfo();
    }

    @Operation(summary = "회원 정보 확인", description = "로그인 된 회원의 정보를 확인합니다.")
    @GetMapping("/{targetId}")
    public MemberFindOneResponse targetInfo(@PathVariable Long targetId) {
        return memberService.findTargetInfo(targetId);
    }

    @Operation(summary = "아이디 중복 체크", description = "아이디 중복 체크를 진행합니다.")
    @PostMapping("/check-username")
    public ResponseEntity<Void> memberUsernameCheck(
            @Valid @RequestBody UsernameCheckRequest request) {
        memberService.checkUsername(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "닉네임 중복 체크", description = "닉네임 중복 체크를 진행합니다.")
    @PostMapping("/check-nickname")
    public ResponseEntity<Void> memberNicknameCheck(
            @Valid @RequestBody NicknameCheckRequest request) {
        memberService.checkNickname(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "닉네임으로 회원 검색", description = "닉네임으로 회원을 검색합니다.")
    @GetMapping("/search")
    public List<MemberSearchResponse> memberNicknameSearch(@RequestParam String nickname) {
        return memberService.searchMemberNickname(nickname);
    }

    // TODO: 테스트 코드 작성 필요
    @Operation(summary = "회원 탈퇴", description = "회원탈퇴를 진행합니다.")
    @DeleteMapping("/withdrawal")
    public ResponseEntity<Void> memberWithdrawal(@Valid @RequestBody UsernameCheckRequest request) {
        memberService.withdrawal(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "소셜 로그인 정보 조회하기", description = "소셜 로그인 정보를 조회합니다.")
    @GetMapping("/me/social")
    public ResponseEntity<MemberSocialInfoResponse> memberSocialInfoFind() {
        MemberSocialInfoResponse response = memberService.findMemberSocialInfo();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 닉네임 변경", description = "회원 닉네임을 변경합니다.")
    @PutMapping("/me/nickname")
    public ResponseEntity<Void> memberNicknameUpdate(
            @Valid @RequestBody NicknameUpdateRequest reqest) {
        memberService.updateMemberNickname(reqest);
        return ResponseEntity.ok().build();
    }
}
