package com.texinsite.engine;

import com.texinsite.engine.model.Document;
import com.texinsite.engine.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class TexInsiteApplication {

    @Autowired
    private DocumentRepository documentRepository;

    public static void main(String[] args) {
        SpringApplication.run(TexInsiteApplication.class, args);
    }

    // 每天凌晨2点清理7天前删除的文档
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredDocuments() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<Document> expiredDocs = documentRepository.findAll().stream()
                .filter(doc -> doc.getDeletedAt() != null && doc.getDeletedAt().isBefore(cutoff))
                .toList();

        for (Document doc : expiredDocs) {
            try {
                // 删除物理文件
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(doc.getFilePath()));
                // 删除数据库记录
                documentRepository.delete(doc);
                System.out.println("已清理过期文档: " + doc.getFilename());
            } catch (Exception e) {
                System.err.println("清理文档失败: " + doc.getFilename() + " - " + e.getMessage());
            }
        }
    }
}
