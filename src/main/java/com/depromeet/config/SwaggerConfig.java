package com.depromeet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.components(authSetting())
			.info(swaggerInfo());
	}

	private Components authSetting() {
		return new Components()
			.addSecuritySchemes(
				"bearer auth",
				new SecurityScheme()
					.type(Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.in(In.HEADER)
					.name("Authorization Token"));
	}

	private Info swaggerInfo() {
		License license = new License();
		license.setUrl("https://github.com/depromeet/10mm-server");
		license.setName("10MM Server Repository");

		return new Info()
			.version("v0.0.1")
			.title("\"10MM 서버 API문서\"")
			.description("10MM 서버 API 문서입니다.")
			.license(license);
	}

}
