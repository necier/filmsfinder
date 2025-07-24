package com.example.filmsfinder.config;

import com.example.filmsfinder.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 启用方法级别的安全注解
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 身份验证管理器，用于登录认证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                    // 1. 禁用 CSRF，因为使用JWT，是无状态的
                .csrf(AbstractHttpConfigurer::disable)
                // 2. 配置 Session 管理策略为 STATELESS，从不创建 Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 3. 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许对登录、注册接口的匿名访问
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // 允许对所有电影信息的 GET 请求（列表、详情）
                        .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                        // 管理员权限：电影的增(POST)、改(PUT)、删(DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/movies").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasAuthority("ADMIN")
                        // 管理员权限：下载链接的增(POST)、删(DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/movies/*/downloadlinks/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/*/downloadlinks/**").hasAuthority("ADMIN")
                        // 登录用户权限：发表评论 (POST)，获取个人信息(GET)
                        .requestMatchers(HttpMethod.POST, "/api/movies/*/comments").authenticated()
                        .requestMatchers("/api/auth/me", "/api/auth/logout").authenticated()
                        // 其他所有请求都需要身份认证
                        .anyRequest().authenticated()
                )

                // 4. 将jwt认证过滤器添加到 Spring Security 过滤器链中
                // 它会在处理用户名密码认证的过滤器之前执行
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}