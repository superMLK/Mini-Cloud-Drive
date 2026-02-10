package com.example.miniclouddrive.service;

import com.example.miniclouddrive.dto.response.CreateFolderResponse;
import com.example.miniclouddrive.dto.response.FileResponse;
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
import org.springframework.data.domain.*;
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
@Transactional(readOnly = true)
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    /**
     * 上傳檔案
     */
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, Long folderId, Integer duplicateAction, Long userId) {
        // ... (保持原樣)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("使用者不存在"));

        checkStorageQuota(user, file.getSize());

        FileEntity parentFolder = validateAndGetFolder(folderId, userId);

        String originalFilename = file.getOriginalFilename();
        Optional<FileEntity> existingFile = fileRepository.findByNameAndParentAndOwnerIdAndDeletedAtIsNull(
                originalFilename, parentFolder, userId);

        if (existingFile.isPresent()) {
            return handleDuplicateFile(file, existingFile.get(), parentFolder, duplicateAction, userId);
        }

        return saveNewFile(file, parentFolder, userId);
    }

    // ... (其他的依舊) ...

    /**
     * 重新命名資料夾
     * 
     * @param folderId 資料夾 ID
     * @param newName  新資料夾名稱
     * @param userId   當前使用者 ID
     */
    @Transactional
    public void renameFolder(Long folderId, String newName, Long userId) {
        // 1. 查詢資料夾並驗證權限
        FileEntity folder = fileRepository
                .findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(folderId, userId, FileType.FOLDER)
                .orElseThrow(() -> new InvalidFolderException(folderId));

        String oldName = folder.getName();

        // 2. 如果名稱沒有變更，直接返回
        if (oldName.equals(newName)) {
            return;
        }

        // 3. 檢查同一父目錄下是否已有同名資料夾
        Optional<FileEntity> existingFolder = fileRepository.findByNameAndParentAndOwnerIdAndDeletedAtIsNull(
                newName, folder.getParent(), userId);

        if (existingFolder.isPresent()) {
            throw new FileAlreadyExistsException(
                    existingFolder.get().getId(),
                    existingFolder.get().getName(),
                    existingFolder.get().getCreatedAt());
        }

        // 4. 更新資料夾名稱
        folder.setName(newName);
        fileRepository.save(folder);

        log.info("資料夾重新命名成功: userId={}, folderId={}, oldName={}, newName={}",
                userId, folder.getId(), oldName, newName);
    }

    /**
     * 建立資料夾
     * 
     * @param name     資料夾名稱
     * @param parentId 父資料夾 ID（null 表示根目錄）
     * @param userId   當前使用者 ID
     * @return 建立結果
     */
    @Transactional
    public CreateFolderResponse createFolder(String name, Long parentId, Long userId) {
        // 1. 驗證父資料夾
        FileEntity parentFolder = validateAndGetFolder(parentId, userId);

        // 2. 檢查同名資料夾是否已存在
        Optional<FileEntity> existingFolder = fileRepository.findByNameAndParentAndOwnerIdAndDeletedAtIsNull(
                name, parentFolder, userId);

        if (existingFolder.isPresent()) {
            throw new FileAlreadyExistsException(
                    existingFolder.get().getId(),
                    existingFolder.get().getName(),
                    existingFolder.get().getCreatedAt());
        }

        // 3. 建立資料夾記錄
        FileEntity folder = FileEntity.builder()
                .name(name)
                .type(FileType.FOLDER)
                .size(0L)
                .parent(parentFolder)
                .ownerId(userId)
                .build();

        FileEntity savedFolder = fileRepository.save(folder);
        log.info("資料夾建立成功: userId={}, folderId={}, folderName={}", userId, savedFolder.getId(), savedFolder.getName());

        return CreateFolderResponse.builder()
                .folderId(savedFolder.getId())
                .name(savedFolder.getName())
                .parentId(parentId)
                .build();
    }

    /**
     * 刪除資料夾（軟刪除）
     * 遞迴刪除資料夾及其所有子項目
     * 
     * @param folderId 資料夾 ID
     * @param userId   當前使用者 ID
     */
    @Transactional
    public void deleteFolder(Long folderId, Long userId) {
        // 1. 查詢資料夾並驗證權限
        FileEntity folder = fileRepository
                .findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(folderId, userId, FileType.FOLDER)
                .orElseThrow(() -> new InvalidFolderException(folderId));

        // 2. 遞迴刪除所有子項目
        deleteRecursively(folder);

        log.info("資料夾刪除成功: userId={}, folderId={}, folderName={}", userId, folder.getId(), folder.getName());
    }

    /**
     * 重新命名資料夾
     * 
     * @param folderId 資料夾 ID
     * @param newName  新資料夾名稱
     * @param userId   當前使用者 ID
     */

    /**
     * 遞迴軟刪除資料夾及其子項目
     */
    private void deleteRecursively(FileEntity entity) {
        // 如果是資料夾，先刪除所有子項目
        if (entity.getType() == FileType.FOLDER) {
            java.util.List<FileEntity> children = fileRepository.findByParentAndDeletedAtIsNull(entity);
            for (FileEntity child : children) {
                deleteRecursively(child);
            }
        }

        // 軟刪除當前項目
        entity.setDeletedAt(java.time.LocalDateTime.now());
        fileRepository.save(entity);

        // 如果是檔案，也可以選擇刪除實體檔案（這裡保留檔案以便未來恢復）
        // if (entity.getType() == FileType.FILE && entity.getFilePath() != null) {
        // try {
        // fileStorageService.delete(entity.getFilePath());
        // } catch (IOException e) {
        // log.warn("刪除實體檔案失敗: {}", entity.getFilePath(), e);
        // }
        // }
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

    /**
     * 查詢檔案列表
     * 
     * @param userId   使用者 ID
     * @param folderId 資料夾 ID
     * @param page     分頁頁碼
     * @param size     每頁筆數
     * @return 檔案列表分頁
     */
    public Page<FileResponse> getFileList(
            Long userId, Long folderId, int page, int size) {
        FileEntity parentFolder = validateAndGetFolder(folderId, userId);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("type").descending()
                        .and(Sort.by("createdAt").descending()));

        Page<FileEntity> filePage = fileRepository.findFiles(parentFolder, userId,
                pageable);

        java.util.List<FileResponse> responseList = filePage.getContent()
                .stream()
                .map(this::buildFileResponse)
                .collect(java.util.stream.Collectors.toList());

        return new PageImpl<>(responseList, pageable, filePage.getTotalElements());
    }

    private FileResponse buildFileResponse(FileEntity fileEntity) {
        return FileResponse.builder()
                .fileId(fileEntity.getId())
                .fileName(fileEntity.getName())
                .size(fileEntity.getSize())
                .uploadTime(fileEntity.getCreatedAt())
                .build();
    }
}
