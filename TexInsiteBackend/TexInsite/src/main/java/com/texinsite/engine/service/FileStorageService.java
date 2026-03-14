package com.texinsite.engine.service;

/**
 * @author Duan Guanyi
 * @version 1.0.0
 * @date 2026/3/12
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件存储服务
 * 负责将用户上传的 PDF 文件安全地保存到服务器本地磁盘中，并生成唯一的文件名以防止覆盖。
 */
@Service
public class FileStorageService {

    @Value("${texinsite.upload.dir:uploads/}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            // 确保目录存在
            Path uploadPath = Paths.get(uploadDir);
            if(!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名，防止重名覆盖
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".pdf";
            String newFileName = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path targetLocation = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation);

            return targetLocation.toString();
        } catch (Exception e) {
            throw new RuntimeException("无法存储文件，请重试！" + e.getMessage());
        }
    }
}
