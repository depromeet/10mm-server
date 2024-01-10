package com.depromeet.global.config.security;

import static org.springframework.security.config.Customizer.*;

import com.depromeet.global.common.constants.SwaggerUrlConstants;
import com.depromeet.global.common.constants.UrlConstants;
import com.depromeet.global.security.CustomOidcAuthenticationSuccessHandler;
import com.depromeet.global.security.CustomOidcUserService;
import com.depromeet.global.security.JwtAuthenticationFilter;
import com.depromeet.global.util.SpringEnvironmentUtil;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final SpringEnvironmentUtil springEnvironmentUtil;
    private final CustomOidcUserService customOidcUserService;
    private final CustomOidcAuthenticationSuccessHandler customOidcAuthenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${swagger.user}")
    private String swaggerUser;

    @Value("${swagger.password}")
    private String swaggerPassword;

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        UserDetails user =
                User.withUsername(swaggerUser)
                        .password(passwordEncoder().encode(swaggerPassword))
                        .roles("SWAGGER")
                        .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.formLogin(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (springEnvironmentUtil.isProdAndDevProfile()) {
            http.authorizeHttpRequests(
                            authorize ->
                                    authorize
                                            .requestMatchers(
                                                    HttpMethod.GET,
                                                    Arrays.stream(SwaggerUrlConstants.values())
                                                            .map(SwaggerUrlConstants::getValue)
                                                            .toArray(String[]::new))
                                            .authenticated())
                    .httpBasic(withDefaults());
        }

        http.authorizeHttpRequests(
                authorize ->
                        authorize
                                .requestMatchers(
                                        HttpMethod.GET,
                                        Arrays.stream(SwaggerUrlConstants.values())
                                                .map(SwaggerUrlConstants::getValue)
                                                .toArray(String[]::new))
                                .permitAll()
                                .requestMatchers("/10mm-actuator/**")
                                .permitAll() // 액추에이터
                                .requestMatchers("/v1/**")
                                .permitAll() // 임시로 모든 요청 허용
                                .requestMatchers("/oauth2/**")
                                .permitAll()
                                .anyRequest()
                                // TODO: 임시로 모든 url 허용했지만, OIDC에서 권한따라 authentication 할 수 있도록 변경 필요
                                .authenticated());
        //        .permitAll());

        http.oauth2Login(
                oauth2 ->
                        oauth2.userInfoEndpoint(
                                        userInfo -> userInfo.oidcUserService(customOidcUserService))
                                .successHandler(customOidcAuthenticationSuccessHandler));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOriginPattern(UrlConstants.PROD_DOMAIN_URL.getValue());

        if (!springEnvironmentUtil.isProdProfile()) {
            configuration.addAllowedOriginPattern(UrlConstants.LOCAL_DOMAIN_URL.getValue());
        }

        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
