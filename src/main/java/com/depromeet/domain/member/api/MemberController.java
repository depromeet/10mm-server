package com.depromeet.domain.member.api;

import com.depromeet.domain.auth.dto.request.UsernameCheckRequest;
import com.depromeet.domain.member.application.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1-2. [회원]", description = "회원 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "아이디 중복 체크", description = "아이디 중복 체크를 진행합니다.")
    @PostMapping("/check-username")
    public ResponseEntity<Void> memberUsernameCheck(
            @Valid @RequestBody UsernameCheckRequest request) {
        memberService.checkUsername(request);
        return ResponseEntity.ok().build();
    }

    // TODO: 테스트 코드 작성 필요
    @Operation(summary = "회원 탈퇴", description = "회원탈퇴를 진행합니다.")
    @DeleteMapping("/withdrawal")
    public ResponseEntity<Void> memberWithdrawal(@Valid @RequestBody UsernameCheckRequest request) {
        memberService.withdrawal(request);
        return ResponseEntity.ok().build();
    }
}
