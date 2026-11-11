package com.example.assetperform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import com.example.assetperform.common.utils.AliOssUtil;

@SpringBootTest 
public class OSSTest {
    
    @Autowired
    private AliOssUtil aliOssUtil;
    
    @Test
    void testUpload() {
        // 模拟一个文本文件上传测试
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello-oss.txt",
                "text/plain",
                "Hello, Alibaba Cloud OSS! 资产管理系统文件上传成功！".getBytes()
        );
        String fileUrl = aliOssUtil.upload(file);
        System.out.println("\n🎉====== 阿里云 OSS 文件上传成功 ======");
        System.out.println("公网访问地址: " + fileUrl);
        System.out.println("=====================================\n");
    }
}
