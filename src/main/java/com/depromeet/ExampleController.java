package com.depromeet;

import com.depromeet.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "etc. [health-check]", description = "상태 체크")
@RestController
public class ExampleController {

    @Operation(summary = "Health Check", description = "API health check")
    @GetMapping("/v1/health-check")
    public ResponseEntity<ErrorResponse> hello() {
        return ResponseEntity.ok(ErrorResponse.of("", "Hello World!"));
    }
}
