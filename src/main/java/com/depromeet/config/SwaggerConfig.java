package com.depromeet.config;

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
import jakarta.servlet.ServletContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.version}")
    private String version;

    @Bean
    public OpenAPI openAPI(ServletContext servletContext) {
        Server server = new Server().url(servletContext.getContextPath());
        return new OpenAPI().servers(List.of(server)).components(authSetting()).info(swaggerInfo());
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
                .version("v" + version)
                .title("\"10MM 서버 API문서\"")
                .description("10MM 서버 API 문서입니다.")
                .license(license);
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        // 객체 직렬화
        return new ModelResolver(objectMapper);
    }
}
