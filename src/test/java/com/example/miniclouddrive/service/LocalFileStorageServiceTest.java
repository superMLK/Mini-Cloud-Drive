package com.example.miniclouddrive.service;

import com.example.miniclouddrive.config.FileStorageProperties;
import com.example.miniclouddrive.exception.FileStorageException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * LocalFileStorageService 單元測試
 * 測試本地檔案儲存服務
 */
@ExtendWith(MockitoExtension.class)
class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private FileStorageProperties fileStorageProperties;

    private LocalFileStorageService localFileStorageService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        when(fileStorageProperties.getUploadDir()).thenReturn(tempDir.toString());
        localFileStorageService = new LocalFileStorageService(fileStorageProperties);
        localFileStorageService.init();
    }

    @Nested
    @DisplayName("store 測試")
    class StoreTests {

        @Test
        @DisplayName("成功儲存檔案")
        void shouldStoreFileSuccessfully() throws IOException {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-file.txt",
                    "text/plain",
                    "Hello, World!".getBytes());

            // When
            String storedPath = localFileStorageService.store(file, USER_ID);

            // Then
            assertThat(storedPath).isNotNull();
            assertThat(storedPath).startsWith(USER_ID + "/");
            assertThat(storedPath).contains("test-file.txt");

            // 驗證檔案確實存在
            Path fullPath = tempDir.resolve(storedPath);
            assertThat(Files.exists(fullPath)).isTrue();
            assertThat(Files.readString(fullPath)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("儲存檔案失敗 - 檔名為空")
        void shouldThrowExceptionWhenFilenameIsEmpty() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "",
                    "text/plain",
                    "content".getBytes());

            // When & Then
            assertThatThrownBy(() -> localFileStorageService.store(file, USER_ID))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("檔案名稱不能為空");
        }

        @Test
        @DisplayName("儲存檔案失敗 - 無效的檔案路徑")
        void shouldThrowExceptionWhenPathContainsDoubleDot() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "../malicious.txt",
                    "text/plain",
                    "malicious content".getBytes());

            // When & Then
            assertThatThrownBy(() -> localFileStorageService.store(file, USER_ID))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("無效的檔案路徑");
        }

        @Test
        @DisplayName("同一使用者多次儲存產生不同檔名")
        void shouldGenerateUniqueFilenamesForSameUser() throws IOException {
            // Given
            MockMultipartFile file1 = new MockMultipartFile(
                    "file", "same-name.txt", "text/plain", "content1".getBytes());
            MockMultipartFile file2 = new MockMultipartFile(
                    "file", "same-name.txt", "text/plain", "content2".getBytes());

            // When
            String path1 = localFileStorageService.store(file1, USER_ID);
            String path2 = localFileStorageService.store(file2, USER_ID);

            // Then
            assertThat(path1).isNotEqualTo(path2);
            assertThat(path1).contains("same-name.txt");
            assertThat(path2).contains("same-name.txt");
        }
    }

    @Nested
    @DisplayName("load 測試")
    class LoadTests {

        @Test
        @DisplayName("成功載入檔案")
        void shouldLoadFileSuccessfully() throws IOException {
            // Given - 先儲存一個檔案
            MockMultipartFile file = new MockMultipartFile(
                    "file", "load-test.txt", "text/plain", "load content".getBytes());
            String storedPath = localFileStorageService.store(file, USER_ID);

            // When
            Resource resource = localFileStorageService.load(storedPath);

            // Then
            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();
        }

        @Test
        @DisplayName("載入檔案失敗 - 檔案不存在")
        void shouldThrowExceptionWhenFileNotFound() {
            // Given
            String nonExistentPath = USER_ID + "/non-existent-file.txt";

            // When & Then
            assertThatThrownBy(() -> localFileStorageService.load(nonExistentPath))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("找不到檔案");
        }
    }

    @Nested
    @DisplayName("delete 測試")
    class DeleteTests {

        @Test
        @DisplayName("成功刪除檔案")
        void shouldDeleteFileSuccessfully() throws IOException {
            // Given - 先儲存一個檔案
            MockMultipartFile file = new MockMultipartFile(
                    "file", "to-delete.txt", "text/plain", "delete me".getBytes());
            String storedPath = localFileStorageService.store(file, USER_ID);
            Path fullPath = tempDir.resolve(storedPath);
            assertThat(Files.exists(fullPath)).isTrue();

            // When
            localFileStorageService.delete(storedPath);

            // Then
            assertThat(Files.exists(fullPath)).isFalse();
        }

        @Test
        @DisplayName("刪除不存在的檔案不拋例外")
        void shouldNotThrowExceptionWhenDeletingNonExistentFile() throws IOException {
            // Given
            String nonExistentPath = USER_ID + "/non-existent.txt";

            // When & Then - 不應該拋出例外
            localFileStorageService.delete(nonExistentPath);
        }
    }
}
