package com.example.miniclouddrive.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity //標記該類為JPA實體
@Table(name = "users") //指定對應的數據庫表名
@Data //自動生成getter、setter、toString、equals、hashCode、包含final的構造子
@NoArgsConstructor //自動生成無參構造子
@AllArgsConstructor //自動生成包含所有參數的構造子
@Builder //使用建造者模式創建對象
public class User {
    /** 主鍵 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //自動生成主鍵值
    private Long id;

    /** 電子郵件 */
    @Column(unique = true, nullable = false)
    private String email;

    /** 密碼雜湊值 */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** 使用者名稱 */
    @Column(unique = true, nullable = false)
    private String username;

    /** 儲存配額，預設為5GB */
    @Column(name = "storage_quota")
    private Long storageQuota;

    /** 帳號創建時間 */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 當執行INSERT操作前，自動設置創建時間 */
    @PrePersist //在INSERT之前觸發
    protected void onCreate() {
        //ex. 執行 userRepository.save(user) 時，JPA 會在 INSERT 之前自動呼叫 onCreate()，將 createdAt 設為當前時間，以及將 storageQuota 設為5GB
        createdAt = LocalDateTime.now();
        storageQuota = 5368709120L; // 確保預設為5GB
    }
}
