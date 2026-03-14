package com.texinsite.engine.service;

import org.springframework.stereotype.Service;

/**
 * 大模型问答功能已移除，该类仅保留以避免对其它模块的编译依赖。
 */
@Service
public class RagChatService {

    public String chatWithDocument(Long docId, String question) {
        return "该项目已移除大模型功能，无法提供智能问答。";
    }
}
