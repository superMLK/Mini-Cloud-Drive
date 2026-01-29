package com.example.miniclouddrive.service;

import com.example.miniclouddrive.dto.response.CreateFolderResponse;
import com.example.miniclouddrive.entity.FileEntity;
import com.example.miniclouddrive.enums.FileType;
import com.example.miniclouddrive.exception.FileAlreadyExistsException;
import com.example.miniclouddrive.exception.InvalidFolderException;
import com.example.miniclouddrive.repository.FileRepository;
import com.example.miniclouddrive.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FileService 單元測試
 * 測試檔案管理相關業務邏輯
 */
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

        @Mock
        private FileRepository fileRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private FileStorageService fileStorageService;

        @InjectMocks
        private FileService fileService;

        private static final Long USER_ID = 1L;
        private static final Long FOLDER_ID = 100L;
        private static final Long PARENT_FOLDER_ID = 50L;

        @Nested
        @DisplayName("createFolder 測試")
        class CreateFolderTests {

                @Test
                @DisplayName("成功建立資料夾 - 根目錄")
                void shouldCreateFolderInRootDirectory() {
                        // Given
                        String folderName = "新資料夾";
                        FileEntity savedFolder = FileEntity.builder()
                                        .id(FOLDER_ID)
                                        .name(folderName)
                                        .type(FileType.FOLDER)
                                        .size(0L)
                                        .ownerId(USER_ID)
                                        .build();

                        when(fileRepository.findByNameAndParentAndOwnerIdAndDeletedAtIsNull(
                                        folderName, null, USER_ID)).thenReturn(Optional.empty());
                        when(fileRepository.save(any(FileEntity.class))).thenReturn(savedFolder);

                        // When
                        CreateFolderResponse response = fileService.createFolder(folderName, null, USER_ID);

                        // Then
                        assertThat(response).isNotNull();
                        assertThat(response.getFolderId()).isEqualTo(FOLDER_ID);
                        assertThat(response.getName()).isEqualTo(folderName);
                        assertThat(response.getParentId()).isNull();

                        verify(fileRepository).save(any(FileEntity.class));
                }

                @Test
                @DisplayName("成功建立資料夾 - 子目錄")
                void shouldCreateFolderInSubDirectory() {
                        // Given
                        String folderName = "子資料夾";
                        FileEntity parentFolder = FileEntity.builder()
                                        .id(PARENT_FOLDER_ID)
                                        .name("父資料夾")
                                        .type(FileType.FOLDER)
                                        .ownerId(USER_ID)
                                        .build();

                        FileEntity savedFolder = FileEntity.builder()
                                        .id(FOLDER_ID)
                                        .name(folderName)
                                        .type(FileType.FOLDER)
                                        .size(0L)
                                        .parent(parentFolder)
                                        .ownerId(USER_ID)
                                        .build();

                        when(fileRepository.findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(
                                        PARENT_FOLDER_ID, USER_ID, FileType.FOLDER))
                                        .thenReturn(Optional.of(parentFolder));
                        when(fileRepository.findByNameAndParentAndOwnerIdAndDeletedAtIsNull(
                                        folderName, parentFolder, USER_ID)).thenReturn(Optional.empty());
                        when(fileRepository.save(any(FileEntity.class))).thenReturn(savedFolder);

                        // When
                        CreateFolderResponse response = fileService.createFolder(folderName, PARENT_FOLDER_ID, USER_ID);

                        // Then
                        assertThat(response).isNotNull();
                        assertThat(response.getFolderId()).isEqualTo(FOLDER_ID);
                        assertThat(response.getName()).isEqualTo(folderName);
                        assertThat(response.getParentId()).isEqualTo(PARENT_FOLDER_ID);
                }

                @Test
                @DisplayName("建立資料夾失敗 - 同名資料夾已存在")
                void shouldThrowExceptionWhenFolderAlreadyExists() {
                        // Given
                        String folderName = "已存在資料夾";
                        FileEntity existingFolder = FileEntity.builder()
                                        .id(200L)
                                        .name(folderName)
                                        .type(FileType.FOLDER)
                                        .ownerId(USER_ID)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(fileRepository.findByNameAndParentAndOwnerIdAndDeletedAtIsNull(
                                        folderName, null, USER_ID)).thenReturn(Optional.of(existingFolder));

                        // When & Then
                        assertThatThrownBy(() -> fileService.createFolder(folderName, null, USER_ID))
                                        .isInstanceOf(FileAlreadyExistsException.class);

                        verify(fileRepository, never()).save(any());
                }

                @Test
                @DisplayName("建立資料夾失敗 - 父資料夾不存在")
                void shouldThrowExceptionWhenParentFolderNotFound() {
                        // Given
                        Long invalidParentId = 999L;

                        when(fileRepository.findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(
                                        invalidParentId, USER_ID, FileType.FOLDER)).thenReturn(Optional.empty());

                        // When & Then
                        assertThatThrownBy(() -> fileService.createFolder("測試", invalidParentId, USER_ID))
                                        .isInstanceOf(InvalidFolderException.class);

                        verify(fileRepository, never()).save(any());
                }
        }

        @Nested
        @DisplayName("deleteFolder 測試")
        class DeleteFolderTests {

                @Test
                @DisplayName("成功刪除空資料夾")
                void shouldDeleteEmptyFolder() {
                        // Given
                        FileEntity folder = FileEntity.builder()
                                        .id(FOLDER_ID)
                                        .name("待刪除資料夾")
                                        .type(FileType.FOLDER)
                                        .ownerId(USER_ID)
                                        .build();

                        when(fileRepository.findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(
                                        FOLDER_ID, USER_ID, FileType.FOLDER)).thenReturn(Optional.of(folder));
                        when(fileRepository.findByParentAndDeletedAtIsNull(folder)).thenReturn(Collections.emptyList());

                        // When
                        fileService.deleteFolder(FOLDER_ID, USER_ID);

                        // Then
                        verify(fileRepository).save(folder);
                        assertThat(folder.getDeletedAt()).isNotNull();
                }

                @Test
                @DisplayName("成功刪除資料夾及其子項目")
                void shouldDeleteFolderWithChildren() {
                        // Given
                        FileEntity folder = FileEntity.builder()
                                        .id(FOLDER_ID)
                                        .name("父資料夾")
                                        .type(FileType.FOLDER)
                                        .ownerId(USER_ID)
                                        .build();

                        FileEntity childFile = FileEntity.builder()
                                        .id(101L)
                                        .name("子檔案.txt")
                                        .type(FileType.FILE)
                                        .parent(folder)
                                        .ownerId(USER_ID)
                                        .build();

                        when(fileRepository.findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(
                                        FOLDER_ID, USER_ID, FileType.FOLDER)).thenReturn(Optional.of(folder));
                        when(fileRepository.findByParentAndDeletedAtIsNull(folder))
                                        .thenReturn(java.util.List.of(childFile));

                        // When
                        fileService.deleteFolder(FOLDER_ID, USER_ID);

                        // Then
                        verify(fileRepository, times(2)).save(any(FileEntity.class));
                        assertThat(folder.getDeletedAt()).isNotNull();
                        assertThat(childFile.getDeletedAt()).isNotNull();
                }

                @Test
                @DisplayName("刪除資料夾失敗 - 資料夾不存在")
                void shouldThrowExceptionWhenFolderNotFound() {
                        // Given
                        Long invalidFolderId = 999L;

                        when(fileRepository.findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(
                                        invalidFolderId, USER_ID, FileType.FOLDER)).thenReturn(Optional.empty());

                        // When & Then
                        assertThatThrownBy(() -> fileService.deleteFolder(invalidFolderId, USER_ID))
                                        .isInstanceOf(InvalidFolderException.class);

                        verify(fileRepository, never()).save(any());
                }
        }

        @Nested
        @DisplayName("renameFolder 測試")
        class RenameFolderTests {

                @Test
                @DisplayName("成功重新命名資料夾")
                void shouldRenameFolderSuccessfully() {
                        // Given
                        String oldName = "舊名稱";
                        String newName = "新名稱";
                        FileEntity folder = FileEntity.builder()
                                        .id(FOLDER_ID)
                                        .name(oldName)
                                        .type(FileType.FOLDER)
                                        .ownerId(USER_ID)
                                        .build();

                        when(fileRepository.findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(
                                        FOLDER_ID, USER_ID, FileType.FOLDER)).thenReturn(Optional.of(folder));
                        when(fileRepository.findByNameAndParentAndOwnerIdAndDeletedAtIsNull(
                                        newName, folder.getParent(), USER_ID)).thenReturn(Optional.empty());
                        when(fileRepository.save(folder)).thenReturn(folder);

                        // When
                        fileService.renameFolder(FOLDER_ID, newName, USER_ID);

                        // Then
                        assertThat(folder.getName()).isEqualTo(newName);
                        verify(fileRepository).save(folder);
                }

                @Test
                @DisplayName("重新命名時名稱相同 - 不做任何操作")
                void shouldDoNothingWhenNameIsSame() {
                        // Given
                        String sameName = "相同名稱";
                        FileEntity folder = FileEntity.builder()
                                        .id(FOLDER_ID)
                                        .name(sameName)
                                        .type(FileType.FOLDER)
                                        .ownerId(USER_ID)
                                        .build();

                        when(fileRepository.findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(
                                        FOLDER_ID, USER_ID, FileType.FOLDER)).thenReturn(Optional.of(folder));

                        // When
                        fileService.renameFolder(FOLDER_ID, sameName, USER_ID);

                        // Then
                        verify(fileRepository, never()).save(any());
                }

                @Test
                @DisplayName("重新命名失敗 - 同名資料夾已存在")
                void shouldThrowExceptionWhenNewNameAlreadyExists() {
                        // Given
                        String oldName = "舊名稱";
                        String newName = "已存在名稱";
                        FileEntity folder = FileEntity.builder()
                                        .id(FOLDER_ID)
                                        .name(oldName)
                                        .type(FileType.FOLDER)
                                        .ownerId(USER_ID)
                                        .build();

                        FileEntity existingFolder = FileEntity.builder()
                                        .id(200L)
                                        .name(newName)
                                        .type(FileType.FOLDER)
                                        .ownerId(USER_ID)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(fileRepository.findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(
                                        FOLDER_ID, USER_ID, FileType.FOLDER)).thenReturn(Optional.of(folder));
                        when(fileRepository.findByNameAndParentAndOwnerIdAndDeletedAtIsNull(
                                        newName, folder.getParent(), USER_ID)).thenReturn(Optional.of(existingFolder));

                        // When & Then
                        assertThatThrownBy(() -> fileService.renameFolder(FOLDER_ID, newName, USER_ID))
                                        .isInstanceOf(FileAlreadyExistsException.class);

                        verify(fileRepository, never()).save(any());
                }

                @Test
                @DisplayName("重新命名失敗 - 資料夾不存在")
                void shouldThrowExceptionWhenFolderNotFound() {
                        // Given
                        Long invalidFolderId = 999L;

                        when(fileRepository.findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(
                                        invalidFolderId, USER_ID, FileType.FOLDER)).thenReturn(Optional.empty());

                        // When & Then
                        assertThatThrownBy(() -> fileService.renameFolder(invalidFolderId, "新名稱", USER_ID))
                                        .isInstanceOf(InvalidFolderException.class);

                        verify(fileRepository, never()).save(any());
                }
        }
}
