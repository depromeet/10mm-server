package com.depromeet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "health-check", description = "상태 체크")
@RequestMapping("/")
@RestController
public class ExampleController {

	@Operation(summary = "게시글 생성", description = "API test")
	@GetMapping("/health-check")
	public String hello() {
		return "hello";
	}
}
