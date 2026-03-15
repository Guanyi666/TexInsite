package com.texinsite.engine.controller;

import com.texinsite.engine.service.RagChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 问答控制器
 * 提供基于文档内容的智能问答功能
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagChatService ragChatService;

    /**
     * 基于指定文档进行问答
     * @param docId 文档ID
     * @param question 用户问题
     * @return AI 生成的回答
     */
    @PostMapping
    public ResponseEntity<String> chat(
            @RequestParam("doc_id") Long docId,
            @RequestBody ChatRequest request) {

        if (docId == null || docId <= 0) {
            return ResponseEntity.badRequest().body("文档ID无效");
        }

        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("问题内容不能为空");
        }

        try {
            String answer = ragChatService.chatWithDocument(docId, request.getQuestion().trim());
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error("问答处理失败", e);
            return ResponseEntity.internalServerError().body("处理问题时出现错误，请稍后重试");
        }
    }

    /**
     * 问答请求DTO
     */
    public static class ChatRequest {
        private String question;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }
}
