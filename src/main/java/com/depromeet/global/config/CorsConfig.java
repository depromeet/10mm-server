package com.depromeet.global.config;

import java.util.ArrayList;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        ArrayList<String> allowedOriginPatterns = new ArrayList<>();
        allowedOriginPatterns.add("https://10mm.today");
        allowedOriginPatterns.add("https://dev.10mm.today");

        String[] patterns = allowedOriginPatterns.toArray(String[]::new);
        registry.addMapping("/**")
                .allowedMethods("*")
                .allowedOriginPatterns(patterns)
                .allowedHeaders("Authorization", "Content-Type")
                .exposedHeaders("Set-Cookie")
                .allowCredentials(true)
                // preflight request에 대한 응답을 캐시할 수 있는 시간
                .maxAge(3600);
    }
}
