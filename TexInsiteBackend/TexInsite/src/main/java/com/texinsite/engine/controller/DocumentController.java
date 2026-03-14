package com.texinsite.engine.controller;

import com.texinsite.engine.model.Document;
import com.texinsite.engine.service.impl.DocumentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentServiceImpl documentService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            // 从 Spring Security 上下文中获取当前经过 JWT 认证的用户名
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            System.out.println("====== JWT解析成功！当前访问的用户名是: [" + username + "] ======");

            Document savedDoc = documentService.uploadAndParseDocument(file, username);
            return ResponseEntity.ok(savedDoc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("上传失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocumentMetadata(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Document updatedDoc = documentService.updateDocumentMetadata(username, id, updates.get("title"), updates.get("author"));
            return ResponseEntity.ok(updatedDoc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteDocument(@PathVariable Long id) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            documentService.softDeleteDocument(username, id);
            return ResponseEntity.ok(Map.of("message", "文档已移至回收站"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<?> restoreDocument(@PathVariable Long id) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            documentService.restoreDocument(username, id);
            return ResponseEntity.ok(Map.of("message", "文档已恢复"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("恢复失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentDeleteDocument(@PathVariable Long id) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            documentService.permanentDeleteDocument(username, id);
            return ResponseEntity.ok(Map.of("message", "文档已彻底删除"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("彻底删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/trash")
    public ResponseEntity<?> listTrashDocuments() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Document> docs = documentService.getUserTrashDocuments(username);
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取回收站失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listDocuments() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Document> docs = documentService.getUserDocuments(username);
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取文档列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/preview/{id}")
    public ResponseEntity<?> previewDocument(@PathVariable Long id) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Document doc = documentService.getDocumentForUser(id, username);
            return ResponseEntity.ok(Map.of(
                    "id", doc.getId(),
                    "title", doc.getTitle(),
                    "filename", doc.getFilename(),
                    "content", doc.getContent()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("预览失败: " + e.getMessage());
        }
    }

    @GetMapping("/raw/{id}")
    public ResponseEntity<?> rawDocument(@PathVariable Long id) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Document doc = documentService.getDocumentForUser(id, username);
            java.nio.file.Path path = java.nio.file.Paths.get(doc.getFilePath());
            if (!java.nio.file.Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            org.springframework.core.io.Resource resource = new org.springframework.core.io.PathResource(path);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFilename() + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("原始文件预览失败: " + e.getMessage());
        }
    }
}
