package com.example.miniclouddrive.entity;

import com.example.miniclouddrive.enums.FileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {
    /** 主鍵 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 檔案名稱 */
    @Column(nullable = false)
    private String name;

    /** 檔案類型：FILE（檔案） 或 FOLDER（資料夾） */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType type;

    /** 檔案大小（以位元組為單位），資料夾預設為0 */
    @Column(nullable = false)
    private Long size;

    /** 檔案儲存路徑，資料夾為NULL */
    @Column(name = "file_path")
    private String filePath;

    /** 父資料夾，根目錄為NULL */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FileEntity parent;

    /** 擁有者ID */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /** 創建時間 */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 刪除時間（軟刪除） */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (size == null) {
            size = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
