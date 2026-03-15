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
 * 将文档内容分块并向量化存储到向量数据库中
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeIngestionService {

    private final VectorStore vectorStore;

    /**
     * 将文档内容向量化并存储到向量数据库
     * @param rawText 文档的原始文本内容
     * @param docId 文档ID
     */
    public void ingestDocument(String rawText, Long docId) {
        if (rawText == null || rawText.trim().isEmpty()) {
            log.warn("文档内容为空，跳过向量化 - 文档ID: {}", docId);
            return;
        }

        try {
            log.info("开始文档向量化 - 文档ID: {}, 文本长度: {}", docId, rawText.length());

            // 1. 文本分块处理
            List<String> chunks = splitTextIntoChunks(rawText, 1000, 200); // 每块1000字符，重叠200字符

            // 2. 创建文档对象列表
            List<Document> documents = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                Document document = new Document(
                    chunks.get(i),
                    Map.of(
                        "document_id", String.valueOf(docId),
                        "chunk_index", String.valueOf(i),
                        "total_chunks", String.valueOf(chunks.size())
                    )
                );
                documents.add(document);
            }

            // 3. 批量存储到向量数据库
            vectorStore.add(documents);

            log.info("文档向量化完成 - 文档ID: {}, 分块数量: {}", docId, chunks.size());

        } catch (Exception e) {
            log.error("文档向量化失败 - 文档ID: {}, 错误: {}", docId, e.getMessage(), e);
            throw new RuntimeException("文档向量化处理失败", e);
        }
    }

    /**
     * 文本分块方法
     * @param text 原始文本
     * @param chunkSize 每块大小
     * @param overlap 重叠大小
     * @return 分块后的文本列表
     */
    private List<String> splitTextIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            // 尝试在句子边界或段落边界分割
            if (end < text.length()) {
                // 查找最近的句子结束符
                int lastSentenceEnd = findLastSentenceEnd(text, start, end);
                if (lastSentenceEnd > start) {
                    end = lastSentenceEnd;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // 计算下一个块的起始位置（考虑重叠）
            start = end - overlap;
            if (start >= text.length()) {
                break;
            }
        }

        return chunks;
    }

    /**
     * 查找句子结束位置
     */
    private int findLastSentenceEnd(String text, int start, int end) {
        String[] sentenceEndings = {". ", "! ", "? ", "。\n", "！\n", "？\n", "\n\n"};

        for (int i = end - 1; i >= start; i--) {
            for (String ending : sentenceEndings) {
                if (i + ending.length() <= text.length() &&
                    text.substring(i, i + ending.length()).equals(ending)) {
                    return i + ending.length();
                }
            }
        }

        return end; // 如果没找到句子边界，返回原始end
    }
}
