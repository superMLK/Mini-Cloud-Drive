# Mini Cloud-Drive

一個以 **Spring Boot** 打造的微型雲端儲存系統，涵蓋企業級後端開發的核心實踐：RESTful API 設計、JWT 認證授權、檔案 I/O 管理、以及完整的例外處理與資料驗證機制。

---

## 技術棧

| 分類 | 技術 |
|------|------|
| **後端框架** | Spring Boot 4.x (Java 17) |
| **資料庫** | MySQL 8.0+、Spring Data JPA |
| **安全機制** | Spring Security + JWT（jjwt 0.12.3） |
| **API 文件** | SpringDoc OpenAPI 3.0（Swagger UI） |
| **容器化** | Docker + Docker Compose |
| **建構工具** | Maven |
| **其他** | Lombok、Bean Validation (JSR-380)、AOP、Spring Actuator |

---

## 核心功能

### 使用者認證
- **註冊 / 登入**：Email + 密碼認證，密碼使用 BCrypt（強度 10）加密
- **JWT Token**：Stateless 架構，Token 有效期 24 小時
- **Security Filter Chain**：自訂 `JwtAuthenticationFilter` 攔截所有受保護路由

### 檔案管理
- **檔案上傳**：支援 Multipart 上傳（單檔最大 50MB），含儲存配額檢查
- **重複檔案處理**：三策略設計 — 拒絕上傳 / 覆蓋現有檔案 / 自動加後綴（如 `file(1).pdf`）
- **資料夾 CRUD**：建立、刪除（遞迴軟刪除）、重新命名
- **檔案列表**：分頁查詢，支援依資料夾篩選

### 架構設計亮點
- **介面化儲存層**：`FileStorageService` 介面 + `LocalFileStorageService` 實作，未來可無縫切換至 S3 / MinIO
- **統一回應格式**：`ApiResponseCode<T>` 包裝所有 API 回應（rtnCode / rtnMsg / data）
- **全域例外處理**：`GlobalExceptionHandler` 統一攔截業務例外、驗證錯誤、檔案大小超限等 7 種異常類型
- **軟刪除機制**：`deleted_at` 時間戳記，檔案刪除後可恢復
- **遞迴資料夾結構**：`FileEntity` 自參照關聯（parent_id），支援無限層級巢狀

---

## API 總覽

| 方法 | 路徑 | 說明 |
|------|------|------|
| `POST` | `/api/auth/register` | 使用者註冊 |
| `POST` | `/api/auth/login` | 使用者登入（回傳 JWT） |
| `POST` | `/api/files/upload` | 上傳檔案 |
| `GET` | `/api/files/list` | 檔案列表（分頁） |
| `POST` | `/api/files/createFolder` | 建立資料夾 |
| `POST` | `/api/files/deleteFolder` | 刪除資料夾（遞迴） |
| `POST` | `/api/files/renameFolder` | 重新命名資料夾 |

> 完整 API 文件請啟動後存取 **Swagger UI**：`http://localhost:8080/swagger-ui.html`

---

## 專案結構

```
src/main/java/com/example/miniclouddrive/
├── config/             # SecurityConfig、OpenApiConfig、FileStorageProperties
├── controller/         # AuthController、FileController
├── service/            # AuthService、FileService、FileStorageService（介面）、LocalFileStorageService
├── repository/         # UserRepository、FileRepository
├── entity/             # User、FileEntity（自參照 parent 結構）
├── dto/
│   ├── request/        # RegisterRequest、LoginRequest、FileUploadRequest、CreateFolderRequest...
│   └── response/       # ApiResponseCode、LoginResponse、FileUploadResponse、FileResponse...
├── security/           # JwtTokenProvider、JwtAuthenticationFilter、CustomUserDetails
├── exception/          # GlobalExceptionHandler + 5 種自訂例外
├── enums/              # FileType（FILE / FOLDER）
└── util/               # SecurityUtils（取得當前認證使用者）
```

---

## 快速開始

### 環境需求
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### 啟動步驟

```bash
# 1. 複製環境變數
cp .env.example .env

# 2. 啟動 MySQL
docker compose up -d

# 3. 啟動後端
./mvnw spring-boot:run

# 4. 開啟 Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## Roadmap: Future Enhancements

本專案持續迭代中，以下為規劃中的擴充方向：

- **檔案下載與預覽** — Stream 方式回傳檔案內容，支援圖片 / PDF 預覽
- **檔案分享機制** — 產生可設定有效期限與密碼保護的分享連結
- **RBAC 權限系統** — 以 AOP 切面實作細粒度檔案權限（Owner / Viewer 角色）
- **搜尋與排序** — 依名稱、類型、日期等條件搜尋，支援多欄位排序
- **路徑麵包屑** — 遞迴 CTE 查詢計算完整目錄路徑
- **第三方雲端儲存** — 擴充 `FileStorageService` 介面以支援 AWS S3 / MinIO
- **前端介面** — React 極簡實作，整合檔案拖曳上傳與即時進度顯示

---

## License

本專案為個人 Side Project，用於技術學習與展示。
