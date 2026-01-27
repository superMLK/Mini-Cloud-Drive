package com.example.miniclouddrive.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 檔案儲存服務介面
 * 抽象化儲存層，未來可擴展至 S3 或其他雲端儲存
 */
public interface FileStorageService {

    /**
     * 儲存檔案
     * 
     * @param file   上傳的檔案
     * @param userId 使用者 ID（用於建立使用者專屬目錄）
     * @return 檔案儲存路徑
     * @throws IOException 檔案 I/O 錯誤
     */
    String store(MultipartFile file, Long userId) throws IOException;

    /**
     * 載入檔案
     * 
     * @param filePath 檔案路徑
     * @return 檔案資源
     * @throws IOException 檔案 I/O 錯誤
     */
    Resource load(String filePath) throws IOException;

    /**
     * 刪除檔案
     * 
     * @param filePath 檔案路徑
     * @throws IOException 檔案 I/O 錯誤
     */
    void delete(String filePath) throws IOException;
}
