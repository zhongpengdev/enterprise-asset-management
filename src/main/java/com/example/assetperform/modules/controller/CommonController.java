package com.example.assetperform.modules.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.assetperform.common.result.Result;
import com.example.assetperform.common.utils.AliOssUtil;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/common")
@RequiredArgsConstructor 
public class CommonController {
    
    private final AliOssUtil aliOssUtil;

    /**
     * 通用文件上传接口
     */
    @PostMapping ("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = aliOssUtil.upload(file);
        return Result.success(url);
    }
}
