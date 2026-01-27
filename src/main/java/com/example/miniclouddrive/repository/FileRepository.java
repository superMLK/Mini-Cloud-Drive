package com.example.miniclouddrive.repository;

import com.example.miniclouddrive.entity.FileEntity;
import com.example.miniclouddrive.enums.FileType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FileRepository extends JpaRepository<@NonNull FileEntity, @NonNull Long> {

    Optional<FileEntity> findByName(String fileName);

    /**
     * 計算使用者已使用的儲存空間
     * 
     * @param ownerId 使用者 ID
     * @return 已使用空間（bytes），若無檔案則回傳 0
     */
    @Query("SELECT COALESCE(SUM(f.size), 0) FROM FileEntity f WHERE f.ownerId = :ownerId AND f.deletedAt IS NULL")
    Long calculateUsedStorageByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 查詢指定資料夾下是否有重複檔名的檔案
     * 
     * @param name    檔案名稱
     * @param parent  父資料夾（null 表示根目錄）
     * @param ownerId 使用者 ID
     * @return 符合條件的檔案
     */
    Optional<FileEntity> findByNameAndParentAndOwnerIdAndDeletedAtIsNull(
            String name, FileEntity parent, Long ownerId);

    /**
     * 查詢指定使用者的指定資料夾是否存在
     * 
     * @param id      資料夾 ID
     * @param ownerId 使用者 ID
     * @param type    類型（應為 FOLDER）
     * @return 符合條件的資料夾
     */
    Optional<FileEntity> findByIdAndOwnerIdAndTypeAndDeletedAtIsNull(
            Long id, Long ownerId, FileType type);
}
