package com.depromeet.global.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable);

		http.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("10mm-actuator/**").permitAll()	// 액추에이터
				.requestMatchers("/v1/**").permitAll()	// 임시로 모든 요청 허용
				.anyRequest().authenticated()
		);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.addAllowedOriginPattern("https://10mm.today");

		// TODO: 운영환경에 따라 허용되는 도메인이 달라지도록 개선
		configuration.addAllowedOriginPattern("http://localhost:3000");

		configuration.addAllowedHeader("*");
		configuration.addAllowedMethod("*");
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public UserDetailsService userDetailsService() {
		// OIDC 구현 전까지 임시로 사용할 유저
		UserDetails mockUser = new PrincipalDetails(1L, "ROLE_USER");

		return new InMemoryUserDetailsManager(mockUser);
	}
}
