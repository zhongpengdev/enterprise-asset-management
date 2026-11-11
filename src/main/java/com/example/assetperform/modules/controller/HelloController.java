package com.example.assetperform.modules.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.assetperform.common.result.Result;

@RestController 
public class HelloController {
    @GetMapping ("/hello")
    public Result<String> hello() {
        throw new RuntimeException("测试全局异常处理");
    }
}
