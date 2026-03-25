package com.texinsite.engine.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeIngestionServiceTest {

    @Test
    void splitTextIntoChunksShouldAlwaysMoveForward() {
        KnowledgeIngestionService service = new KnowledgeIngestionService(new NoOpVectorStore());
        String text = "Short.\n\n" + "A".repeat(40) + "Tail segment.";

        List<String> chunks = service.splitTextIntoChunks(text, 20, 19);

        assertTrue(chunks.size() > 1, "text should be split into multiple chunks");
        assertTrue(chunks.size() <= text.length(), "chunk count should remain bounded");
        assertTrue(chunks.getFirst().startsWith("Short."), "first chunk should preserve the leading sentence");
        assertTrue(chunks.getLast().contains("Tail segment."), "last chunk should include the tail content");
    }

    private static final class NoOpVectorStore implements VectorStore {

        @Override
        public void add(List<Document> documents) {
        }

        @Override
        public void delete(List<String> idList) {
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            return List.of();
        }
    }
}
