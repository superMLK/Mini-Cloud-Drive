package com.example.miniclouddrive.service;

import com.example.miniclouddrive.dto.response.FileUploadResponse;
import com.example.miniclouddrive.entity.FileEntity;
import com.example.miniclouddrive.entity.User;
import com.example.miniclouddrive.enums.FileType;
import com.example.miniclouddrive.exception.FileAlreadyExistsException;
import com.example.miniclouddrive.exception.FileStorageException;
import com.example.miniclouddrive.exception.InsufficientStorageException;
import com.example.miniclouddrive.exception.InvalidFolderException;
import com.example.miniclouddrive.repository.FileRepository;
import com.example.miniclouddrive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * 檔案管理服務
 * 處理檔案上傳、下載、刪除等業務邏輯
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    /**
     * 上傳檔案
     * 
     * @param file            上傳的檔案
     * @param folderId        目標資料夾 ID（null 表示根目錄）
     * @param duplicateAction 重複檔案處理方式（null=拒絕, 0=覆蓋, 1=加後綴）
     * @param userId          當前使用者 ID
     * @return 上傳結果
     */
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, Long folderId, Integer duplicateAction, Long userId) {
        // 1. 取得使用者資訊
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("使用者不存在"));

        // 2. 檢查儲存配額
        checkStorageQuota(user, file.getSize());

        // 3. 驗證並取得目標資料夾
        FileEntity parentFolder = validateAndGetFolder(folderId, userId);

        // 4. 檢查檔案是否重複
        String originalFilename = file.getOriginalFilename();
        Optional<FileEntity> existingFile = fileRepository.findByNameAndParentAndOwnerIdAndDeletedAtIsNull(
                originalFilename, parentFolder, userId);

        if (existingFile.isPresent()) {
            // 處理重複檔案
            return handleDuplicateFile(file, existingFile.get(), parentFolder, duplicateAction, userId);
        }

        // 5. 儲存檔案並建立記錄
        return saveNewFile(file, parentFolder, userId);
    }

    /**
     * 檢查儲存配額是否足夠
     */
    private void checkStorageQuota(User user, long fileSize) {
        Long usedStorage = fileRepository.calculateUsedStorageByOwnerId(user.getId());
        Long remainingQuota = user.getStorageQuota() - usedStorage;

        if (fileSize > remainingQuota) {
            throw new InsufficientStorageException(fileSize, remainingQuota);
        }
    }

    /**
     * 驗證並取得目標資料夾
     * 
     * @return 資料夾實體，若 folderId 為 null 則回傳 null（表示根目錄）
     */
    private FileEntity validateAndGetFolder(Long folderId, Long userId) {
        if (folderId == null) {
            return null; // 根目錄
        }

        return fileRepository.findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(folderId, userId, FileType.FOLDER)
                .orElseThrow(() -> new InvalidFolderException(folderId));
    }

    /**
     * 處理重複檔案
     */
    private FileUploadResponse handleDuplicateFile(MultipartFile file, FileEntity existingFile,
            FileEntity parentFolder, Integer duplicateAction, Long userId) {
        if (duplicateAction == null) {
            // 拒絕上傳，拋出例外讓前端顯示選項
            throw new FileAlreadyExistsException(
                    existingFile.getId(),
                    existingFile.getName(),
                    existingFile.getCreatedAt());
        } else if (duplicateAction == 0) {
            // 覆蓋：刪除舊檔案後上傳新檔案
            return replaceExistingFile(file, existingFile, userId);
        } else if (duplicateAction == 1) {
            // 加後綴：產生新檔名後上傳
            return saveFileWithSuffix(file, parentFolder, userId);
        } else {
            throw new IllegalArgumentException("無效的 duplicateAction 值: " + duplicateAction);
        }
    }

    /**
     * 覆蓋現有檔案
     */
    private FileUploadResponse replaceExistingFile(MultipartFile file, FileEntity existingFile, Long userId) {
        try {
            // 刪除舊的實體檔案
            if (existingFile.getFilePath() != null) {
                fileStorageService.delete(existingFile.getFilePath());
            }

            // 儲存新檔案
            String filePath = fileStorageService.store(file, userId);

            // 更新資料庫記錄
            existingFile.setFilePath(filePath);
            existingFile.setSize(file.getSize());
            FileEntity savedFile = fileRepository.save(existingFile);

            return buildResponse(savedFile);
        } catch (IOException e) {
            throw new FileStorageException("覆蓋檔案失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 儲存檔案並自動加後綴
     */
    private FileUploadResponse saveFileWithSuffix(MultipartFile file, FileEntity parentFolder, Long userId) {
        String originalFilename = file.getOriginalFilename();
        String newFilename = generateUniqueFilename(originalFilename, parentFolder, userId);

        try {
            // 儲存實體檔案
            String filePath = fileStorageService.store(file, userId);

            // 建立資料庫記錄
            FileEntity fileEntity = FileEntity.builder()
                    .name(newFilename)
                    .type(FileType.FILE)
                    .size(file.getSize())
                    .filePath(filePath)
                    .parent(parentFolder)
                    .ownerId(userId)
                    .build();

            FileEntity savedFile = fileRepository.save(fileEntity);
            return buildResponse(savedFile);
        } catch (IOException e) {
            throw new FileStorageException("儲存檔案失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 產生唯一檔名（加後綴）
     * 例如：report.pdf -> report(1).pdf -> report(2).pdf
     */
    private String generateUniqueFilename(String originalFilename, FileEntity parentFolder, Long userId) {
        String baseName;
        String extension;

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = originalFilename.substring(0, dotIndex);
            extension = originalFilename.substring(dotIndex);
        } else {
            baseName = originalFilename;
            extension = "";
        }

        int counter = 1;
        String newFilename = baseName + "(" + counter + ")" + extension;

        // 持續檢查直到找到不重複的檔名
        while (fileRepository.findByNameAndParentAndOwnerIdAndDeletedAtIsNull(newFilename, parentFolder, userId)
                .isPresent()) {
            counter++;
            newFilename = baseName + "(" + counter + ")" + extension;
        }

        return newFilename;
    }

    /**
     * 儲存新檔案
     */
    private FileUploadResponse saveNewFile(MultipartFile file, FileEntity parentFolder, Long userId) {
        try {
            // 儲存實體檔案
            String filePath = fileStorageService.store(file, userId);

            // 建立資料庫記錄
            FileEntity fileEntity = FileEntity.builder()
                    .name(file.getOriginalFilename())
                    .type(FileType.FILE)
                    .size(file.getSize())
                    .filePath(filePath)
                    .parent(parentFolder)
                    .ownerId(userId)
                    .build();

            FileEntity savedFile = fileRepository.save(fileEntity);
            log.info("檔案上傳成功: userId={}, fileId={}, fileName={}", userId, savedFile.getId(), savedFile.getName());

            return buildResponse(savedFile);
        } catch (IOException e) {
            throw new FileStorageException("儲存檔案失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 建立回應 DTO
     */
    private FileUploadResponse buildResponse(FileEntity fileEntity) {
        return FileUploadResponse.builder()
                .fileId(fileEntity.getId())
                .fileName(fileEntity.getName())
                .size(fileEntity.getSize())
                .uploadTime(fileEntity.getCreatedAt())
                .build();
    }
}
