package com.texinsite.engine.controller;

import com.texinsite.engine.dto.ShareLinkRequest;
import com.texinsite.engine.model.ShareLink;
import com.texinsite.engine.repository.DocumentCommentRepository;
import com.texinsite.engine.service.ShareLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    @Autowired
    private ShareLinkService shareLinkService;

    @Autowired
    private DocumentCommentRepository documentCommentRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createShareLink(@RequestBody ShareLinkRequest request) {
        try {
            if (request.getDocumentId() == null) {
                return ResponseEntity.badRequest().body("documentId 不能为空");
            }
            int expireMinutes = request.getExpireMinutes() != null ? request.getExpireMinutes() : 60;
            Integer maxDownloads = request.getMaxDownloads();
            Boolean includeComments = request.getIncludeComments() != null ? request.getIncludeComments() : false;

            // 这里使用 Spring Security 的上下文获取用户名（必须登录才能生成分享链接）
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(401).body("请先登录才能生成分享链接");
            }
            String username = auth.getName();
            var link = shareLinkService.createShareLink(username, request.getDocumentId(), expireMinutes, maxDownloads, includeComments);

            return ResponseEntity.ok(Map.of(
                    "token", link.getToken(),
                    "url", "/api/share/" + link.getToken(),
                    "expireAt", link.getExpireAt(),
                    "maxDownloads", link.getMaxDownloads(),
                    "downloadCount", link.getDownloadCount(),
                    "includeComments", link.getIncludeComments(),
                    "documentId", link.getDocument().getId(),
                    "documentTitle", link.getDocument().getTitle(),
                    "documentFilename", link.getDocument().getFilename()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("生成分享链接失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listShareLinks() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(401).body("请先登录");
            }
            String username = auth.getName();
            var list = shareLinkService.listShareLinks(username);

            var payload = list.stream()
                    .filter(link -> link.getDocument() != null)
                    .map(link -> {
                        Map<String, Object> item = new java.util.HashMap<>();
                        item.put("token", link.getToken());
                        item.put("url", "/api/share/" + link.getToken());
                        item.put("expireAt", link.getExpireAt());
                        item.put("maxDownloads", link.getMaxDownloads());
                        item.put("downloadCount", link.getDownloadCount());
                        item.put("includeComments", link.getIncludeComments());
                        item.put("documentId", link.getDocument().getId());
                        item.put("documentTitle", link.getDocument().getTitle());
                        item.put("documentFilename", link.getDocument().getFilename());

                        // 计算剩余次数逻辑
                        Integer remaining = null;
                        if (link.getMaxDownloads() != null) {
                            int count = link.getDownloadCount() == null ? 0 : link.getDownloadCount();
                            remaining = Math.max(0, link.getMaxDownloads() - count);
                        }
                        item.put("remaining", remaining);

                        return item;
                    }).toList();

            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取分享链接列表失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<?> revokeShareLink(@PathVariable String token) {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(401).body("请先登录");
            }
            String username = auth.getName();
            shareLinkService.revokeShareLink(username, token);
            return ResponseEntity.ok(Map.of("message", "分享链接已撤销"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("撤销分享链接失败: " + e.getMessage());
        }
    }

    @GetMapping("/view/{token}")
    public ResponseEntity<?> viewSharedDocument(@PathVariable String token) {
        try {
            // 验证 token 并获取文档
            Path path = shareLinkService.validateTokenAndGetFilePath(token);
            ShareLink shareLink = shareLinkService.getShareLinkByToken(token);

            // 读取文档内容（这里简化处理，实际应该调用文档服务）
            String content = "";
            try {
                content = Files.readString(path);
            } catch (Exception e) {
                content = "该文档暂不支持预览（可能是纯图片PDF）";
            }

            Map<String, Object> response = Map.of(
                "title", shareLink.getDocument().getTitle(),
                "filename", shareLink.getDocument().getFilename(),
                "content", content
            );

            // 如果包含评论，添加评论数据
            if (shareLink.getIncludeComments() != null && shareLink.getIncludeComments()) {
                var comments = documentCommentRepository.findByDocumentIdAndShareWithLinkTrue(shareLink.getDocument().getId());
                var commentData = comments.stream().map(comment -> Map.<String, Object>of(
                    "id", comment.getId(),
                    "content", comment.getContent(),
                    "createdAt", comment.getCreatedAt(),
                    "user", Map.of("username", comment.getUser().getUsername())
                )).toList();
                response = new java.util.HashMap<>(response);
                response.put("comments", commentData);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("分享链接失效: " + e.getMessage());
        }
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> downloadSharedFile(@PathVariable String token) {
        try {
            Path path = shareLinkService.validateTokenAndGetFilePath(token);
            if (path == null || !Files.exists(path) || !Files.isRegularFile(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new PathResource(path);
            String filename = path.getFileName().toString();
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
            try {
                var detected = Files.probeContentType(path);
                if (detected != null) {
                    MediaType parsed = MediaType.parseMediaType(detected);
                    if (parsed != null) {
                        contentType = parsed;
                    }
                }
            } catch (Exception ignored) {
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("分享链接失效: " + e.getMessage());
        }
    }
}
