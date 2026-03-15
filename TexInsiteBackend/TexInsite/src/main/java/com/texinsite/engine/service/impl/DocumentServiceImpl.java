package com.texinsite.engine.service.impl;

import com.texinsite.engine.model.Document;
import com.texinsite.engine.model.User;
import com.texinsite.engine.repository.DocumentRepository;
import com.texinsite.engine.repository.UserRepository;
import com.texinsite.engine.service.FileStorageService;
import com.texinsite.engine.service.KnowledgeIngestionService;
import com.texinsite.engine.service.TikaParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class DocumentServiceImpl {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TikaParserService tikaParserService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KnowledgeIngestionService knowledgeIngestionService;

    @Transactional
    public Document uploadAndParseDocument(MultipartFile file, String username) {
        // 1. 获取当前用户实体
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 存储文件到本地
        String savedPath = fileStorageService.storeFile(file);

        // 3. Tika 提取元数据与文本
        Map<String, String> parsedData = tikaParserService.parsePdf(savedPath);

        // --- 防御性编程：防止 Tika 解析纯图片 PDF 导致内容为 null ---
        String content = parsedData.get("content");
        if (content == null || content.trim().isEmpty()) {
            System.err.println("警告：未能从文件 [" + file.getOriginalFilename() + "] 中提取到文本内容");
            content = "无提取文本内容"; // 赋予默认值，防止后续 length() 报错
        }

        // 4. 构建 Document 实体并保存数据库
        Document doc = new Document();
        doc.setUser(user);
        doc.setFilename(file.getOriginalFilename());
        doc.setFilePath(savedPath);
        doc.setTitle(parsedData.get("title"));
        doc.setAuthor(parsedData.get("author"));
        doc.setYear(parsedData.get("year"));

        // 补充原文内容，便于后续直接在页面左侧高亮或比对
        doc.setContent(content);

        // 截取前 500 个字符作为摘要预览
        doc.setSummary(content.length() > 500 ? content.substring(0, 500) + "..." : content);

        // 5. 先保存到关系型数据库，以获取自增的 Document ID
        Document savedDoc = documentRepository.save(doc);

        // 6. 核心动作：将文本切片并利用大模型生成 Embedding，存入 pgvector
        try {
            System.out.println("====== 开始对文档 [ID: " + savedDoc.getId() + "] 进行切片与向量化 ======");
            knowledgeIngestionService.ingestDocument(content, savedDoc.getId());
            System.out.println("====== 向量化完成 ======");
        } catch (Exception e) {
            System.err.println("向量化入库失败，请检查 API Key、网络连通性或 pgvector 状态: " + e.getMessage());
            // 根据业务需求，这里可以选择抛出异常让事务回滚，或者允许数据库保留文件记录但仅提示 AI 解析失败。
            // 由于后续还要靠文档本身来查看，这里暂不阻断整体流程。
        }

        return savedDoc;
    }

    public List<Document> getUserDocuments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return documentRepository.findByUserIdAndDeletedAtIsNullOrderByUploadedAtDesc(user.getId());
    }

    public List<Document> getUserTrashDocuments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return documentRepository.findByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(user.getId());
    }

    @Transactional
    public Document updateDocumentMetadata(String username, Long documentId, String title, String author) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Document document = documentRepository.findByIdAndUserId(documentId, user.getId())
                .orElseThrow(() -> new RuntimeException("文档不存在或无权限访问"));

        if (title != null) document.setTitle(title);
        if (author != null) document.setAuthor(author);

        return documentRepository.save(document);
    }

    @Transactional
    public void softDeleteDocument(String username, Long documentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Document document = documentRepository.findByIdAndUserId(documentId, user.getId())
                .orElseThrow(() -> new RuntimeException("文档不存在或无权限访问"));

        if (document.getDeletedAt() != null) {
            throw new RuntimeException("文档已在回收站中");
        }

        document.setDeletedAt(java.time.LocalDateTime.now());
        documentRepository.save(document);
    }

    @Transactional
    public void restoreDocument(String username, Long documentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Document document = documentRepository.findByIdAndUserId(documentId, user.getId())
                .orElseThrow(() -> new RuntimeException("文档不存在或无权限访问"));

        if (document.getDeletedAt() == null) {
            throw new RuntimeException("文档不在回收站中");
        }

        document.setDeletedAt(null);
        documentRepository.save(document);
    }

    @Transactional
    public void permanentDeleteDocument(String username, Long documentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Document document = documentRepository.findByIdAndUserId(documentId, user.getId())
                .orElseThrow(() -> new RuntimeException("文档不存在或无权限访问"));

        // 删除物理文件
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(document.getFilePath()));
        } catch (Exception e) {
            // 忽略文件删除错误
        }

        documentRepository.delete(document);
    }

    public Document getDocumentForUser(Long documentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return documentRepository.findByIdAndUserId(documentId, user.getId())
                .orElseThrow(() -> new RuntimeException("文档不存在或无权限访问"));
    }
}