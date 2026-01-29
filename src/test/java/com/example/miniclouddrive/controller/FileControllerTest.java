package com.example.miniclouddrive.controller;

import com.example.miniclouddrive.dto.request.CreateFolderRequest;
import com.example.miniclouddrive.dto.request.DeleteFolderRequest;
import com.example.miniclouddrive.dto.request.RenameFolderRequest;
import com.example.miniclouddrive.dto.response.CreateFolderResponse;
import com.example.miniclouddrive.exception.FileAlreadyExistsException;
import com.example.miniclouddrive.exception.GlobalExceptionHandler;
import com.example.miniclouddrive.exception.InvalidFolderException;
import com.example.miniclouddrive.service.FileService;
import com.example.miniclouddrive.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FileController 單元測試
 * 測試資料夾管理 API 端點
 */
@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private ObjectMapper objectMapper;

    private static final Long USER_ID = 1L;
    private static final Long FOLDER_ID = 100L;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(fileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/files/createFolder 測試")
    class CreateFolderTests {

        @Test
        @DisplayName("成功建立資料夾")
        void shouldCreateFolderSuccessfully() throws Exception {
            // Given
            CreateFolderRequest request = new CreateFolderRequest("新資料夾", null);
            CreateFolderResponse response = CreateFolderResponse.builder()
                    .folderId(FOLDER_ID)
                    .name("新資料夾")
                    .parentId(null)
                    .build();

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
                when(fileService.createFolder("新資料夾", null, USER_ID)).thenReturn(response);

                // When & Then
                mockMvc.perform(post("/api/files/createFolder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.rtnCode").value("0000"))
                        .andExpect(jsonPath("$.data.folderId").value(FOLDER_ID))
                        .andExpect(jsonPath("$.data.name").value("新資料夾"));

                verify(fileService).createFolder("新資料夾", null, USER_ID);
            }
        }

        @Test
        @DisplayName("建立資料夾失敗 - 名稱為空")
        void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
            // Given
            CreateFolderRequest request = new CreateFolderRequest("", null);

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

                // When & Then
                mockMvc.perform(post("/api/files/createFolder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());

                verify(fileService, never()).createFolder(any(), any(), any());
            }
        }

        @Test
        @DisplayName("建立資料夾失敗 - 同名資料夾已存在")
        void shouldReturnConflictWhenFolderAlreadyExists() throws Exception {
            // Given
            CreateFolderRequest request = new CreateFolderRequest("已存在", null);

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
                when(fileService.createFolder("已存在", null, USER_ID))
                        .thenThrow(new FileAlreadyExistsException(200L, "已存在", LocalDateTime.now()));

                // When & Then
                mockMvc.perform(post("/api/files/createFolder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isConflict());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/files/deleteFolder 測試")
    class DeleteFolderTests {

        @Test
        @DisplayName("成功刪除資料夾")
        void shouldDeleteFolderSuccessfully() throws Exception {
            // Given
            DeleteFolderRequest request = new DeleteFolderRequest(FOLDER_ID);

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
                doNothing().when(fileService).deleteFolder(FOLDER_ID, USER_ID);

                // When & Then
                mockMvc.perform(post("/api/files/deleteFolder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.rtnCode").value("0000"));

                verify(fileService).deleteFolder(FOLDER_ID, USER_ID);
            }
        }

        @Test
        @DisplayName("刪除資料夾失敗 - 資料夾不存在")
        void shouldReturnNotFoundWhenFolderNotExists() throws Exception {
            // Given
            DeleteFolderRequest request = new DeleteFolderRequest(999L);

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
                doThrow(new InvalidFolderException(999L)).when(fileService).deleteFolder(999L, USER_ID);

                // When & Then
                mockMvc.perform(post("/api/files/deleteFolder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/files/renameFolder 測試")
    class RenameFolderTests {

        @Test
        @DisplayName("成功重新命名資料夾")
        void shouldRenameFolderSuccessfully() throws Exception {
            // Given
            RenameFolderRequest request = new RenameFolderRequest();
            request.setId(FOLDER_ID);
            request.setName("新名稱");

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
                doNothing().when(fileService).renameFolder(FOLDER_ID, "新名稱", USER_ID);

                // When & Then
                mockMvc.perform(post("/api/files/renameFolder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.rtnCode").value("0000"));

                verify(fileService).renameFolder(FOLDER_ID, "新名稱", USER_ID);
            }
        }

        @Test
        @DisplayName("重新命名失敗 - 名稱為空")
        void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
            // Given
            RenameFolderRequest request = new RenameFolderRequest();
            request.setId(FOLDER_ID);
            request.setName("");

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

                // When & Then
                mockMvc.perform(post("/api/files/renameFolder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());

                verify(fileService, never()).renameFolder(any(), any(), any());
            }
        }

        @Test
        @DisplayName("重新命名失敗 - 資料夾不存在")
        void shouldReturnNotFoundWhenFolderNotExists() throws Exception {
            // Given
            RenameFolderRequest request = new RenameFolderRequest();
            request.setId(999L);
            request.setName("新名稱");

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
                doThrow(new InvalidFolderException(999L)).when(fileService).renameFolder(999L, "新名稱", USER_ID);

                // When & Then
                mockMvc.perform(post("/api/files/renameFolder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound());
            }
        }

        @Test
        @DisplayName("重新命名失敗 - 同名資料夾已存在")
        void shouldReturnConflictWhenNewNameAlreadyExists() throws Exception {
            // Given
            RenameFolderRequest request = new RenameFolderRequest();
            request.setId(FOLDER_ID);
            request.setName("已存在名稱");

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
                doThrow(new FileAlreadyExistsException(200L, "已存在名稱", LocalDateTime.now()))
                        .when(fileService).renameFolder(FOLDER_ID, "已存在名稱", USER_ID);

                // When & Then
                mockMvc.perform(post("/api/files/renameFolder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isConflict());
            }
        }
    }
}
