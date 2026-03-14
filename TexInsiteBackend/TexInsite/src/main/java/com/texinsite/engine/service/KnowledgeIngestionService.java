package com.texinsite.engine.service;

import org.springframework.stereotype.Service;

/**
 * 文档向量化服务（当前已禁用 AI 向量化功能，仅保留接口以防止依赖出错）
 */
@Service
public class KnowledgeIngestionService {

    public void ingestDocument(String rawText, Long docId) {
        // 当前不启用 AI 向量化（Spring AI 依赖已移除），仅作为占位接口使用。
        System.out.println("[知识向量化已禁用] 文档ID=" + docId + "，原文本长度=" + (rawText == null ? 0 : rawText.length()));
    }
}
