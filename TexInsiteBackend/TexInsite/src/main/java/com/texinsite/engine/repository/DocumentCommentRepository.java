package com.texinsite.engine.repository;

import com.texinsite.engine.model.DocumentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentCommentRepository extends JpaRepository<DocumentComment, Long> {

    List<DocumentComment> findByDocumentIdOrderByCreatedAtAsc(Long documentId);

    List<DocumentComment> findByDocumentIdAndShareWithLinkTrueOrderByCreatedAtAsc(Long documentId);

    List<DocumentComment> findByDocumentIdAndShareWithLinkTrue(long id);
}