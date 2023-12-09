package com.depromeet;

import com.depromeet.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "health-check", description = "상태 체크")
@RestController
public class ExampleController {

    @Operation(summary = "게시글 생성", description = "API health check")
    @GetMapping("/v1/health-check")
    public ResponseEntity<ErrorResponse> hello() {
		return ResponseEntity.ok(ErrorResponse.of(HttpStatus.OK, "Hello World!"));
    }
}
