package com.texinsite.engine.service;

import com.texinsite.engine.model.Document;
import com.texinsite.engine.model.ShareLink;
import com.texinsite.engine.model.User;
import com.texinsite.engine.repository.DocumentRepository;
import com.texinsite.engine.repository.ShareLinkRepository;
import com.texinsite.engine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShareLinkService {

    @Autowired
    private ShareLinkRepository shareLinkRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 为指定用户的文档创建一个一次性或定时过期的分享链接
     */
    @Transactional
    public ShareLink createShareLink(String username, Long documentId, int expireMinutes, Integer maxDownloads, Boolean includeComments) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        // 只能分享自己的文档
        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权分享该文档");
        }

        // 过期时间及下载次数控制
        if (expireMinutes <= 0) {
            expireMinutes = 60;
        }

        ShareLink shareLink = new ShareLink();
        shareLink.setToken(UUID.randomUUID().toString().replace("-", ""));
        shareLink.setDocument(document);
        shareLink.setExpireAt(LocalDateTime.now().plusMinutes(expireMinutes));
        shareLink.setMaxDownloads(maxDownloads != null && maxDownloads > 0 ? maxDownloads : null);
        shareLink.setDownloadCount(0);
        shareLink.setIncludeComments(includeComments != null ? includeComments : false);

        return shareLinkRepository.save(shareLink);
    }

    /**
     * 查询当前用户创建的分享链接列表
     */
    public java.util.List<ShareLink> listShareLinks(String username) {
        return shareLinkRepository.findByDocument_User_Username(username);
    }

    /**
     * 撤销分享链接
     */
    @Transactional
    public void revokeShareLink(String username, String token) {
        ShareLink shareLink = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("分享链接不存在"));

        if (!shareLink.getDocument().getUser().getUsername().equals(username)) {
            throw new RuntimeException("无权撤销该分享链接");
        }

        shareLinkRepository.delete(shareLink);
    }

    /**
     * 根据 token 获取分享链接（用于查看）
     */
    public ShareLink getShareLinkByToken(String token) {
        return shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("分享链接不存在"));
    }

    /**
     * 根据 token 验证是否可下载，并返回文件路径
     */
    @Transactional
    public Path validateTokenAndGetFilePath(String token) {
        ShareLink shareLink = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("分享链接不存在或已失效"));

        if (shareLink.getExpireAt() != null && LocalDateTime.now().isAfter(shareLink.getExpireAt())) {
            throw new RuntimeException("分享链接已过期");
        }

        if (shareLink.getMaxDownloads() != null) {
            if (shareLink.getDownloadCount() != null && shareLink.getDownloadCount() >= shareLink.getMaxDownloads()) {
                throw new RuntimeException("分享链接的下载次数已用尽");
            }
        }

        shareLink.setDownloadCount((shareLink.getDownloadCount() == null ? 0 : shareLink.getDownloadCount()) + 1);
        shareLinkRepository.save(shareLink);

        return Paths.get(shareLink.getDocument().getFilePath());
    }
}
