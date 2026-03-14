package com.texinsite.engine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // 1. 定义密码加密器：强力哈希加密，不可逆
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 允许匿名访问登录和注册接口
                        .requestMatchers("/api/auth/**").permitAll()
                        // 分享文件下载接口无需登录（token 方式）
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/share/*").permitAll()
                        // 其余 Share 相关接口需登录（创建、管理、撤销）
                        .requestMatchers("/api/share/**").authenticated()
                        // 文档相关接口需登录（列表、上传、预览等）
                        .requestMatchers("/api/documents/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);;
        return http.build();
    }
}