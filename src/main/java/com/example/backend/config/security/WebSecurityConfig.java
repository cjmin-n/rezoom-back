package com.example.backend.config.security;

import com.example.backend.config.jwt.JwtAuthFilter;
import com.example.backend.config.jwt.JwtExceptionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();

        JsonUsernamePasswordAuthenticationFilter jsonFilter = new JsonUsernamePasswordAuthenticationFilter(authenticationManager);
        jsonFilter.setAuthenticationSuccessHandler(successHandler);
        jsonFilter.setAuthenticationFailureHandler(failureHandler);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        .requestMatchers(
                                "/auth/login", "/auth/signup", "/auths/token/logout",
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/swagger-resources/**", "/webjars/**",
                                "/v3/api-docs/**",
                                "/api/**"
                            ).permitAll()
                        .requestMatchers(
                                "로그인 해야되는 곳들"
                        ).authenticated()
                        .requestMatchers(
                                "관리자만 접근할 수 있는 곳들"
                        ).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class); // json로그인 처리 - /auth/login (3)
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); //JWT 검증 및 SecurityContext 등록 -> 로그인 이후 요청에 필요함.(2)
        http.addFilterBefore(jwtExceptionFilter, JwtAuthFilter.class);  //JWT 오류 (토큰 만료, 위조 등) 잡기 -> 제일 먼저 실행됨(1)

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("http://localhost:3000", "http://localhost:8080"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // 반드시 true

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
