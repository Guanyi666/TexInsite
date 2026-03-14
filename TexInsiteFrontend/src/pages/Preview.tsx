import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api';
import { useToast } from '../components/ToastContext';

type Comment = {
  id: number;
  content: string;
  createdAt: string;
  updatedAt?: string;
  user: { username: string };
  shareWithLink: boolean;
};

export default function Preview() {
  const { id } = useParams<{ id: string }>();
  const [content, setContent] = useState<string>('');
  const [doc, setDoc] = useState<{ title?: string; filename?: string } | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState('');
  const [shareWithLink, setShareWithLink] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { showToast } = useToast();

  const fetchComments = async () => {
    if (!id) return;
    try {
      const resp = await api.get(`/documents/${id}/comments`);
      setComments(resp.data || []);
    } catch (err) {
      // ignore comment fetch errors
    }
  };

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);

    Promise.all([
      api.get(`/documents/preview/${id}`),
      fetchComments()
    ])
      .then(([previewResp]) => {
        setDoc({ title: previewResp.data.title, filename: previewResp.data.filename });
        setContent(previewResp.data.content || '该文档暂不支持预览（可能是纯图片PDF）');
      })
      .catch((err) => {
        setError(err?.response?.data || '预览失败');
      })
      .finally(() => setLoading(false));
  }, [id]);

  const handleAddComment = async () => {
    if (!id || !newComment.trim()) return;
    try {
      await api.post(`/documents/${id}/comments`, {
        content: newComment.trim(),
        shareWithLink
      });
      setNewComment('');
      setShareWithLink(false);
      fetchComments();
      showToast('评论已添加', 'success');
    } catch (err: any) {
      showToast(err?.response?.data || '添加评论失败', 'error');
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    if (!confirm('确定要删除此评论吗？')) return;
    try {
      await api.delete(`/documents/${id}/comments/${commentId}`);
      fetchComments();
      showToast('评论已删除', 'success');
    } catch (err: any) {
      showToast(err?.response?.data || '删除评论失败', 'error');
    }
  };

  return (
    <div>
      <div className="card">
        <h2>文档预览</h2>
        <p>查看文档内容（文本）或点击「PDF 预览」在新窗口查看原始文件。</p>
      </div>

      {error ? <div className="alert error">{error}</div> : null}

      {loading ? (
        <div className="card">加载中...</div>
      ) : (
        <div className="stack">
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
              <div>
                <h3 style={{ margin: 0 }}>{doc?.title || doc?.filename || '文档'}</h3>
                <div style={{ color: 'rgba(240,246,252,0.7)', marginTop: 6 }}>
                  文档 ID: {id}
                </div>
              </div>
              <button
                className="button"
                onClick={async () => {
                  if (!id) return;
                  try {
                    const resp = await api.get(`/documents/raw/${id}`, { responseType: 'blob' });
                    const blob = new Blob([resp.data], { type: resp.headers['content-type'] || 'application/pdf' });
                    const url = URL.createObjectURL(blob);
                    window.open(url, '_blank');
                    showToast('将在新标签页中打开 PDF 预览', 'info');
                  } catch (err: any) {
                    showToast(err?.response?.data || 'PDF 预览失败，请确保已登录', 'error');
                  }
                }}
              >
                PDF 预览
              </button>
            </div>
            <pre style={{ whiteSpace: 'pre-wrap', marginTop: 16, color: 'rgba(240,246,252,0.85)', maxHeight: 420, overflow: 'auto' }}>
              {content}
            </pre>
          </div>

          <div className="card">
            <h3>评论 ({comments.length})</h3>
            <div style={{ marginTop: 12 }}>
              <textarea
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                className="input"
                placeholder="写下你的灵感或评论..."
                rows={3}
                style={{ resize: 'vertical' }}
              />
              <div style={{ marginTop: 8, display: 'flex', alignItems: 'center', gap: 12 }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <input
                    type="checkbox"
                    checked={shareWithLink}
                    onChange={(e) => setShareWithLink(e.target.checked)}
                  />
                  <span style={{ fontSize: '0.9rem', color: 'rgba(240,246,252,0.7)' }}>
                    随分享链接一起公开
                  </span>
                </label>
                <button className="button primary" onClick={handleAddComment} disabled={!newComment.trim()}>
                  添加评论
                </button>
              </div>
            </div>

            <div style={{ marginTop: 16 }}>
              {comments.length === 0 ? (
                <div style={{ color: 'rgba(240,246,252,0.6)', fontStyle: 'italic' }}>
                  暂无评论
                </div>
              ) : (
                <div className="stack" style={{ gap: '0.75rem' }}>
                  {comments.map((comment) => (
                    <div key={comment.id} style={{ padding: '0.75rem', background: 'rgba(255,255,255,0.05)', borderRadius: 8 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div style={{ flex: 1 }}>
                          <div style={{ fontSize: '0.85rem', color: 'rgba(240,246,252,0.7)', marginBottom: 4 }}>
                            {comment.user.username} • {new Date(comment.createdAt).toLocaleString()}
                            {comment.shareWithLink && (
                              <span style={{ marginLeft: 8, color: 'var(--accent)', fontSize: '0.8rem' }}>
                                (公开)
                              </span>
                            )}
                          </div>
                          <div style={{ whiteSpace: 'pre-wrap', color: 'rgba(240,246,252,0.9)' }}>
                            {comment.content}
                          </div>
                        </div>
                        <button
                          className="button danger"
                          style={{ fontSize: '0.8rem', padding: '0.25rem 0.5rem' }}
                          onClick={() => handleDeleteComment(comment.id)}
                        >
                          删除
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
