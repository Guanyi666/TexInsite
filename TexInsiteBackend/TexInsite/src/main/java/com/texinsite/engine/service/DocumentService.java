package com.texinsite.engine.service;

import com.texinsite.engine.model.Document;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DocumentService {
    Document uploadDocument(MultipartFile file) throws Exception;
    List<Document> getAllDocuments();
}