package com.texinsite.engine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 大模型问答功能已移除，该Controller保留仅避免前端调用产生 404 错误。
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @PostMapping
    public ResponseEntity<String> chat() {
        return ResponseEntity.badRequest().body("大模型问答功能已移除");
    }
}
