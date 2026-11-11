package com.example.assetperform.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component 
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long experation;

    private SecretKey key;

    // Spring 注入完成后自动开始初始化SecretKey
    @PostConstruct 
    public void init(){
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // jwt token 生成
    public String generateToken(Long userId, String username){
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() +  experation))
                .signWith(key)
                .compact();        
    }

    // token校验
    public Claims parseToken(String token){
        try{
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        }catch(Exception e) {
            log.warn("Token解析失败：{}" , e.getMessage());
            return null;
        }
    }
}
