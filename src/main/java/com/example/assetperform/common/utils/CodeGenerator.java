package com.example.assetperform.common.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class CodeGenerator {
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 生成Item商品的唯一编码 WZ前缀 + yyyyMMdd + 每日流水
     */
    public String generateCode(String prefix) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern(
            "yyyyMMdd"
        ));
        String redisKey = "seq:" + prefix + ":" + dateStr;
        
        // redis原子递增
        Long seq = stringRedisTemplate.opsForValue().increment(redisKey);

        // 两天过期，自动清理Key
        if(seq != null && seq == 1L) {
            stringRedisTemplate.expire(redisKey, Duration.ofDays(2));
        }

        // 拼接成标准编码
        return String.format("%s%s%04d", prefix, dateStr, seq != null ? seq : 1L);
    }
}
