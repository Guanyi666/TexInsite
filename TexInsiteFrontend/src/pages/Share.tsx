import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import api from '../api';

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

export default function Share() {
  const query = useQuery();
  const defaultDocId = query.get('docId') || '';
  const [docId, setDocId] = useState(defaultDocId);
  const [expireMinutes, setExpireMinutes] = useState(60);
  const [maxDownloads, setMaxDownloads] = useState<number | ''>('');
  const [includeComments, setIncludeComments] = useState(false);
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    if (defaultDocId) {
      setDocId(defaultDocId);
    }
  }, [defaultDocId]);

  const refreshResult = async (token: string) => {
    try {
      const resp = await api.get('/share/list');
      const found = (resp.data || []).find((item: any) => item.token === token);
      if (found) {
        setResult(found);
      }
    } catch (err) {
      // ignore refresh errors
    }
  };

  const createLink = async () => {
    setError(null);
    setResult(null);
    if (!docId) {
      setError('请填写文档 ID');
      return;
    }

    setLoading(true);
    try {
      const resp = await api.post('/share/create', {
        documentId: Number(docId),
        expireMinutes,
        maxDownloads: maxDownloads === '' ? null : Number(maxDownloads),
        includeComments
      });
      setResult(resp.data);
      // 立即刷新一次，确保显示后端当前下载 / 剩余情况
      await refreshResult(resp.data.token);
    } catch (err: any) {
      setError(err?.response?.data || '生成分享链接失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // 定期刷新当前分享链接的下载次数，防止页面不主动更新造成的显示不同步
    if (!result?.token) return;

    const interval = setInterval(() => {
      refreshResult(result.token);
    }, 3500);

    return () => clearInterval(interval);
  }, [result?.token]);

  const downloadFile = async () => {
    if (!result?.token) return;
    setDownloading(true);

    try {
      const url = result.url;
      const response = await fetch(url, { method: 'GET' });

      if (!response.ok) {
        throw new Error(`下载失败: ${response.status}`);
      }

      const blob = await response.blob();
      const disposition = response.headers.get('content-disposition') || '';
      const filenameMatch = disposition.match(/filename="?([^";]+)"?/);
      const filename = filenameMatch ? filenameMatch[1] : `download-${result.documentId}`;

      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(link.href);

      // Refresh download count
      await refreshResult(result.token);
    } catch (err: any) {
      setError(err?.message || '下载失败');
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: 680 }}>
      <div className="card">
        <h2>生成分享链接</h2>
        <p>通过生成链接，其他人在有效期内可直接下载文件（无需登录）。</p>

        <div className="label">文档 ID</div>
        <input value={docId} onChange={(e) => setDocId(e.target.value)} className="input" />

        <div className="grid-2">
          <div>
            <div className="label">有效期（分钟）</div>
            <input
              type="number"
              value={expireMinutes}
              min={1}
              className="input"
              onChange={(e) => setExpireMinutes(Number(e.target.value))}
            />
          </div>
          <div>
            <div className="label">最大下载次数（空或 0 表示不限）</div>
            <input
              type="number"
              value={maxDownloads}
              min={0}
              className="input"
              onChange={(e) => setMaxDownloads(e.target.value === '' ? '' : Number(e.target.value))}
            />
          </div>
        </div>

        <div style={{ marginTop: 12 }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              checked={includeComments}
              onChange={(e) => setIncludeComments(e.target.checked)}
            />
            <span>包含文档评论</span>
          </label>
        </div>

        {error ? <div className="alert error">{error}</div> : null}

        <button className="button primary" onClick={createLink} disabled={loading || downloading}>
          {loading ? '生成中…' : '生成分享链接'}
        </button>

        {result ? (
          <div className="card" style={{ marginTop: '1.6rem' }}>
            <h3>分享链接已生成</h3>
            <p style={{ wordBreak: 'break-all' }}>
              访问链接：
              <span style={{ color: 'var(--accent)' }}>{window.location.origin + result.url}</span>
            </p>
            <p>过期时间：{new Date(result.expireAt).toLocaleString()}</p>
            <p>最大下载次数：{result.maxDownloads ?? '不限'}</p>
            <p>已下载次数：{result.downloadCount}</p>

            <div style={{ marginTop: 12, display: 'flex', gap: 12, flexWrap: 'wrap' }}>
              <button className="button primary" onClick={downloadFile} disabled={downloading}>
                {downloading ? '下载中…' : '下载文件'}
              </button>
              <button
                className="button"
                onClick={() => {
                  navigator.clipboard.writeText(window.location.origin + result.url);
                }}
              >
                复制链接
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}
