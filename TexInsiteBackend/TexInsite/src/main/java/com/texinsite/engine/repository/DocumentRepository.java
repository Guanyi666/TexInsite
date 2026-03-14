package com.texinsite.engine.repository;

import com.texinsite.engine.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // 基础的 save, findById, delete 等方法已经内置了
    // 按照上传时间倒叙查询某个用户的所有文档（未删除的）
    List<Document> findByUserIdAndDeletedAtIsNullOrderByUploadedAtDesc(Long userId);

    // 查询回收站中的文档（已删除的）
    List<Document> findByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(Long userId);

    java.util.Optional<Document> findByIdAndUserId(Long id, Long userId);
}
