package com.texinsite.engine.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 共享链接实体：生成一个随机 token，用于安全分享文件（可设置有效期和下载次数限制）。
 */
@Data
@Entity
@Table(name = "share_links")
public class ShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 访问分享链接时使用的随机 token
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * 关联的文档
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /**
     * 过期时间（到期后链接无效）
     */
    private LocalDateTime expireAt;

    /**
     * 最大下载次数（null 表示不限次数）
     */
    private Integer maxDownloads;

    /**
     * 已下载次数
     */
    private Integer downloadCount = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 是否包含评论
     */
    private Boolean includeComments = false;
}
