import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api';

type SharedDocument = {
  title: string;
  filename: string;
  content: string;
  comments?: Array<{
    id: number;
    content: string;
    createdAt: string;
    user: { username: string };
  }>;
};

export default function SharedView() {
  const { token } = useParams<{ token: string }>();
  const [data, setData] = useState<SharedDocument | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;
    setLoading(true);
    setError(null);

    api
      .get(`/share/view/${token}`)
      .then((resp) => {
        setData(resp.data);
      })
      .catch((err) => {
        setError(err?.response?.data || '加载共享内容失败');
      })
      .finally(() => setLoading(false));
  }, [token]);

  const downloadFile = async () => {
    if (!token) return;
    try {
      const response = await fetch(`/api/share/${token}`, { method: 'GET' });
      if (!response.ok) {
        throw new Error(`下载失败: ${response.status}`);
      }

      const blob = await response.blob();
      const disposition = response.headers.get('content-disposition') || '';
      const filenameMatch = disposition.match(/filename="?([^";]+)"?/);
      const filename = filenameMatch ? filenameMatch[1] : `download-${token}`;

      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(link.href);
    } catch (err: any) {
      setError(err?.message || '下载失败');
    }
  };

  return (
    <div className="container" style={{ maxWidth: 800 }}>
      <div className="card">
        <h2>共享文档</h2>
        <p>这是通过分享链接访问的文档内容。</p>
      </div>

      {error ? <div className="alert error">{error}</div> : null}

      {loading ? (
        <div className="card">加载中...</div>
      ) : data ? (
        <div className="stack">
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
              <div>
                <h3 style={{ margin: 0 }}>{data.title || data.filename}</h3>
              </div>
              <button className="button primary" onClick={downloadFile}>
                下载文件
              </button>
            </div>
            <pre style={{ whiteSpace: 'pre-wrap', marginTop: 16, color: 'rgba(240,246,252,0.85)', maxHeight: 420, overflow: 'auto' }}>
              {data.content || '该文档暂不支持预览（可能是纯图片PDF）'}
            </pre>
          </div>

          {data.comments && data.comments.length > 0 && (
            <div className="card">
              <h3>文档评论</h3>
              <div style={{ marginTop: 16 }}>
                <div className="stack" style={{ gap: '0.75rem' }}>
                  {data.comments.map((comment) => (
                    <div key={comment.id} style={{ padding: '0.75rem', background: 'rgba(255,255,255,0.05)', borderRadius: 8 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div style={{ flex: 1 }}>
                          <div style={{ fontSize: '0.85rem', color: 'rgba(240,246,252,0.7)', marginBottom: 4 }}>
                            {comment.user.username} • {new Date(comment.createdAt).toLocaleString()}
                          </div>
                          <div style={{ whiteSpace: 'pre-wrap', color: 'rgba(240,246,252,0.9)' }}>
                            {comment.content}
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>
      ) : null}
    </div>
  );
}