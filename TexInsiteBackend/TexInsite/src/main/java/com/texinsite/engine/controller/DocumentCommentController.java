package com.texinsite.engine.controller;

import com.texinsite.engine.model.DocumentComment;
import com.texinsite.engine.service.DocumentCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents/{docId}/comments")
public class DocumentCommentController {

    @Autowired
    private DocumentCommentService commentService;

    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long docId, @RequestBody Map<String, Object> request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String content = (String) request.get("content");
            Boolean shareWithLink = (Boolean) request.get("shareWithLink");

            DocumentComment comment = commentService.addComment(username, docId, content, shareWithLink);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("添加评论失败: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getComments(@PathVariable Long docId) {
        try {
            List<DocumentComment> comments = commentService.getComments(docId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取评论失败: " + e.getMessage());
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long docId, @PathVariable Long commentId, @RequestBody Map<String, Object> request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String content = (String) request.get("content");
            Boolean shareWithLink = (Boolean) request.get("shareWithLink");

            DocumentComment comment = commentService.updateComment(username, commentId, content, shareWithLink);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新评论失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long docId, @PathVariable Long commentId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            commentService.deleteComment(username, commentId);
            return ResponseEntity.ok(Map.of("message", "评论已删除"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除评论失败: " + e.getMessage());
        }
    }
}