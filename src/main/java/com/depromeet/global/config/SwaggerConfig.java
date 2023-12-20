package com.depromeet.global.config;

import static com.depromeet.global.util.SpringEnvironmentUtil.*;

import com.depromeet.global.util.SpringEnvironmentUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    private static final String SERVER_NAME = "10MM";
    private static final String API_TITLE = "10MM 서버 API 문서";
    private static final String API_DESCRIPTION = "10MM 서버 API 문서입니다.";
    private static final String GITHUB_URL = "https://github.com/depromeet/10mm-server";

    private final SpringEnvironmentUtil springEnvironmentUtil;

    @Value("${swagger.version}")
    private String version;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(swaggerServers())
                .components(authSetting())
                .info(swaggerInfo());
    }

    private List<Server> swaggerServers() {
        Server server = new Server().url(getServerUrl()).description(API_DESCRIPTION);
        return List.of(server);
    }

    private String getServerUrl() {
        switch (springEnvironmentUtil.getCurrentProfile()) {
            case "prod":
                return "https://api.10mm.today";
            case "dev":
                return "https://dev-api.10mm.today";
            default:
                return "https://localhost:8080";
        }
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
        license.setUrl(GITHUB_URL);
        license.setName(SERVER_NAME);

        return new Info()
                .version("v" + version)
                .title(API_TITLE)
                .description(API_DESCRIPTION)
                .license(license);
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        // 객체 직렬화
        return new ModelResolver(objectMapper);
    }
}
