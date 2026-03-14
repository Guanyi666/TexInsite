package com.texinsite.engine.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 关联users表的user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private  User user;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String filename;

    @Column(nullable = false)
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String author;
    private String year;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    // 软删除字段：null 表示未删除，否则为删除时间
    private LocalDateTime deletedAt;

    // --- 手动补全所有需要的 Getter 和 Setter ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public String getSummary() {
        return summary;
    }

    public String getYear() {
        return year;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getUploadTime() { return uploadedAt; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadedAt = uploadTime; }

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}