package com.texinsite.engine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文档知识库向量化服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeIngestionService {

    static final int DEFAULT_CHUNK_SIZE = 1000;
    static final int DEFAULT_OVERLAP = 200;
    static final int SENTENCE_SEARCH_WINDOW = 200;
    static final int VECTOR_STORE_BATCH_SIZE = 32;

    private final VectorStore vectorStore;

    public void ingestDocument(String rawText, Long docId) {
        if (rawText == null || rawText.trim().isEmpty()) {
            log.warn("文档内容为空，跳过向量化 - 文档ID: {}", docId);
            return;
        }

        try {
            log.info("开始文档向量化 - 文档ID: {}, 文本长度: {}", docId, rawText.length());

            List<String> chunks = splitTextIntoChunks(rawText, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
            int totalChunks = chunks.size();

            for (int batchStart = 0; batchStart < totalChunks; batchStart += VECTOR_STORE_BATCH_SIZE) {
                int batchEnd = Math.min(batchStart + VECTOR_STORE_BATCH_SIZE, totalChunks);
                List<Document> documents = new ArrayList<>(batchEnd - batchStart);

                for (int i = batchStart; i < batchEnd; i++) {
                    documents.add(new Document(
                            chunks.get(i),
                            Map.of(
                                    "document_id", String.valueOf(docId),
                                    "chunk_index", String.valueOf(i),
                                    "total_chunks", String.valueOf(totalChunks)
                            )
                    ));
                }

                vectorStore.add(documents);
            }

            log.info("文档向量化完成 - 文档ID: {}, 分块数量: {}", docId, totalChunks);
        } catch (Exception e) {
            log.error("文档向量化失败 - 文档ID: {}, 错误: {}", docId, e.getMessage(), e);
            throw new RuntimeException("文档向量化处理失败", e);
        }
    }

    List<String> splitTextIntoChunks(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be greater than 0");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be between 0 and chunkSize - 1");
        }

        List<String> chunks = new ArrayList<>();
        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            if (end < text.length()) {
                int searchStart = Math.max(start, end - Math.min(SENTENCE_SEARCH_WINDOW, chunkSize / 2));
                int boundary = findLastSentenceEnd(text, searchStart, end);
                if (boundary > start) {
                    end = boundary;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end >= text.length()) {
                break;
            }

            start = Math.max(end - overlap, start + 1);
        }

        return chunks;
    }

    private int findLastSentenceEnd(String text, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            char current = text.charAt(i);

            if (current == '.' || current == '!' || current == '?' ||
                    current == '。' || current == '！' || current == '？') {
                return i + 1;
            }

            if (current == '\n' && i > start && text.charAt(i - 1) == '\n') {
                return i + 1;
            }
        }

        return end;
    }
}
