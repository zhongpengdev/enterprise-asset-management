package com.example.assetperform.common.utils;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.example.assetperform.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component 
public class AliOssUtil {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件并返回访问网址
     */
    public String upload(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            throw new BusinessException("上传的图片不能为空");
        }

        // 获取原始文件，生成唯一的uuid文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if(originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName  = UUID.randomUUID().toString().replace("-", "") + extension;

        // 初始化OSS
        OSS oss = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try (InputStream inputStream = file.getInputStream()){
            // 上传文件
            oss.putObject(bucketName, fileName, inputStream);

            // 公网URL
            String fileUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;

            log.info("文件上传阿里云OSS成功，访问地址：{}", fileUrl);

            return fileUrl;
        } catch (Exception e){
            log.error("文件上传阿里云OSS失败", e);
            throw new BusinessException("文件上传失败");
        } finally {
            // 关闭OSS客户端
            oss.shutdown();
        }
    }
}
