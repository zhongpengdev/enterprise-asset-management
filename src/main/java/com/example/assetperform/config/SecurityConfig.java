package com.example.assetperform.config;

import com.example.assetperform.common.result.Result;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.assetperform.common.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration 
@EnableWebSecurity 
@EnableMethodSecurity // 不知道 方法级别的权限拦截注解 @PreAuthorize
@RequiredArgsConstructor 
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                // 开启跨域
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 关闭跨站防护
                .csrf(csrf -> csrf.disable())

                //禁用session
                .sessionManagement((session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ))

                // 配置接口访问权限
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/login").permitAll() // 登陆接口公开白名单
                    .anyRequest().authenticated() // 其他均需要合法token
                )

                // 为登陆或者token失效时统一返回401
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setContentType("application/json;charset=UTF-8");

                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                        Result<?> result = Result.error(401, "未登录或Token已过期，请重新登录！");
                    response.getWriter().write(new ObjectMapper().writeValueAsString(result));
                    })
                )

                // 挂载过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
                return http.build();
    }

    // 跨域配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 密码加密工具
    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}
