package com.texinsite.engine.dto;

/**
 * 创建分享链接请求
 */
public class ShareLinkRequest {

    private Long documentId;

    /**
     * 过期时间（单位：分钟），如果不填则默认 60 分钟
     */
    private Integer expireMinutes;

    /**
     * 最大下载次数（null 或 0 表示不限次数）
     */
    private Integer maxDownloads;

    /**
     * 是否包含评论
     */
    private Boolean includeComments;

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Integer getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(Integer expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    public Integer getMaxDownloads() {
        return maxDownloads;
    }

    public void setMaxDownloads(Integer maxDownloads) {
        this.maxDownloads = maxDownloads;
    }

    public Boolean getIncludeComments() {
        return includeComments;
    }

    public void setIncludeComments(Boolean includeComments) {
        this.includeComments = includeComments;
    }
}
