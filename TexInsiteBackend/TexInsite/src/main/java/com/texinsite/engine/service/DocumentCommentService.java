package com.texinsite.engine.service;

import com.texinsite.engine.model.Document;
import com.texinsite.engine.model.DocumentComment;
import com.texinsite.engine.model.User;
import com.texinsite.engine.repository.DocumentCommentRepository;
import com.texinsite.engine.repository.DocumentRepository;
import com.texinsite.engine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentCommentService {

    @Autowired
    private DocumentCommentRepository commentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public DocumentComment addComment(String username, @NonNull Long documentId, String content, Boolean shareWithLink) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        // 检查文档是否属于用户或公开（这里假设只能评论自己的文档）
        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权评论该文档");
        }

        DocumentComment comment = new DocumentComment();
        comment.setDocument(document);
        comment.setUser(user);
        comment.setContent(content);
        comment.setShareWithLink(shareWithLink != null ? shareWithLink : false);
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public List<DocumentComment> getComments(Long documentId) {
        return commentRepository.findByDocumentIdOrderByCreatedAtAsc(documentId);
    }

    public List<DocumentComment> getSharedComments(Long documentId) {
        return commentRepository.findByDocumentIdAndShareWithLinkTrueOrderByCreatedAtAsc(documentId);
    }

    @Transactional
    public DocumentComment updateComment(String username, @NonNull Long commentId, String content, Boolean shareWithLink) {
        DocumentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("无权编辑该评论");
        }

        comment.setContent(content);
        comment.setShareWithLink(shareWithLink != null ? shareWithLink : comment.getShareWithLink());
        comment.setUpdatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(String username, @NonNull Long commentId) {
        DocumentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("无权删除该评论");
        }

        commentRepository.delete(comment);
    }
}