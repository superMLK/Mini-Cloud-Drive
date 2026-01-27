package com.example.miniclouddrive.service;

import com.example.miniclouddrive.config.FileStorageProperties;
import com.example.miniclouddrive.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地檔案儲存服務
 * 將檔案儲存在本地檔案系統中
 * 儲存路徑結構：{uploadDir}/{userId}/{uuid}_{originalFilename}
 */
@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private final FileStorageProperties fileStorageProperties;
    private Path rootLocation;

    /**
     * 初始化儲存目錄
     */
    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("無法建立儲存目錄: " + rootLocation, e);
        }
    }

    @Override
    public String store(MultipartFile file, Long userId) throws IOException {
        // 取得原始檔名並清理
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // 驗證檔名
        if (originalFilename.isEmpty()) {
            throw new FileStorageException("檔案名稱不能為空");
        }
        if (originalFilename.contains("..")) {
            throw new FileStorageException("無效的檔案路徑: " + originalFilename);
        }

        // 產生唯一檔名：uuid_原始檔名
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // 建立使用者專屬目錄
        Path userDir = rootLocation.resolve(userId.toString());
        Files.createDirectories(userDir);

        // 儲存檔案
        Path targetLocation = userDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // 回傳相對路徑（相對於 rootLocation）
        return userId + "/" + uniqueFilename;
    }

    @Override
    public Resource load(String filePath) throws IOException {
        try {
            Path file = rootLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("找不到檔案: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("無效的檔案路徑: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) throws IOException {
        Path file = rootLocation.resolve(filePath).normalize();
        Files.deleteIfExists(file);
    }
}
