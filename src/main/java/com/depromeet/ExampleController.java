package com.depromeet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

	@GetMapping("/health-check")
	public String hello() {
		return "hello";
	}
}
