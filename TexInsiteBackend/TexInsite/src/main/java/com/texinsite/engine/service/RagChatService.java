package com.texinsite.engine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 基于 RAG（Retrieval-Augmented Generation）的文档问答服务
 * 使用向量检索 + 大模型生成回答
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatService {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    /**
     * 基于文档内容进行问答
     * @param docId 文档ID
     * @param question 用户问题
     * @return AI 生成的回答
     */
    public String chatWithDocument(Long docId, String question) {
        try {
            log.info("开始处理文档问答 - 文档ID: {}, 问题: {}", docId, question);

            // 1. 向量检索相关文档片段
            List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query(question)
                    .topK(5)  // 返回最相关的5个片段
                    .filterExpression("document_id == " + docId)  // 只搜索指定文档
                    .build()
            );

            if (similarDocuments.isEmpty()) {
                log.warn("未找到相关文档片段 - 文档ID: {}", docId);
                return "抱歉，在文档中没有找到与您问题相关的内容。请尝试重新表述问题或确认文档包含相关信息。";
            }

            // 2. 构建上下文
            StringBuilder contextBuilder = new StringBuilder();
            for (Document doc : similarDocuments) {
                contextBuilder.append(doc.getText()).append("\n\n");
            }
            String context = contextBuilder.toString();

            // 3. 构建提示词
            String promptTemplate = """
                基于以下文档内容回答用户的问题。如果文档内容无法回答问题，请说明无法找到相关信息。

                文档内容：
                {context}

                用户问题：{question}

                请用中文回答，并保持回答的准确性和相关性。
                """;

            PromptTemplate template = new PromptTemplate(promptTemplate);
            Prompt prompt = template.create(Map.of(
                "context", context,
                "question", question
            ));

            // 4. 调用AI模型生成回答
            ChatClient chatClient = ChatClient.builder(chatModel).build();
            String response = chatClient.prompt(prompt).call().content();

            log.info("问答处理完成 - 文档ID: {}", docId);
            return response;

        } catch (Exception e) {
            log.error("文档问答处理失败 - 文档ID: {}, 错误: {}", docId, e.getMessage(), e);
            return "抱歉，处理您的问题时出现错误。请稍后重试或联系管理员。";
        }
    }
}
