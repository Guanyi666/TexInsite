import React, { useEffect, useState } from 'react';
import api from '../api';
import { useToast } from '../components/ToastContext';

type ShareItem = {
  token: string;
  url: string;
  expireAt: string | null;
  maxDownloads: number | null;
  downloadCount: number;
  remaining: number | null;
  documentId: number;
  documentTitle: string | null;
  documentFilename: string;
  includeComments?: boolean;
};

export default function ShareManager() {
  const [list, setList] = useState<ShareItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { showToast } = useToast();

  const fetchList = async () => {
    setLoading(true);
    setError(null);
    try {
      const resp = await api.get('/share/list');
      setList(resp.data);
    } catch (err: any) {
      setError(err?.response?.data || '获取分享链接失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();

    const handleFocus = () => {
      // 当用户从下载完成后返回标签页时，自动刷新剩余次数
      fetchList();
    };

    window.addEventListener('focus', handleFocus);
    return () => window.removeEventListener('focus', handleFocus);
  }, []);

  const handleRevoke = async (token: string) => {
    try {
      await api.delete(`/share/${token}`);
      showToast('分享链接已撤销', 'success');
      fetchList();
    } catch (err: any) {
      showToast(err?.response?.data || '撤销失败', 'error');
    }
  };

  return (
    <div>
      <div className="card">
        <h2>分享链接管理</h2>
        <p>可查看当前用户生成的所有分享链接，并支持撤销。</p>
      </div>

      {error ? <div className="alert error">{error}</div> : null}

      {loading ? (
        <div className="card">加载中...</div>
      ) : (
        <div className="stack">
          {list.length === 0 ? (
            <div className="card">暂无分享链接，先去生成一个。</div>
          ) : (
            list.map((item) => (
              <div key={item.token} className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: '0.95rem', color: 'rgba(240,246,252,0.7)' }}>
                      文档：{item.documentTitle || item.documentFilename} (ID: {item.documentId})
                    </div>
                    <div style={{ marginTop: 10, wordBreak: 'break-all' }}>
                      链接：
                      <a href={item.url} target="_blank" rel="noreferrer" style={{ color: 'var(--accent)' }}>
                        {window.location.origin + item.url}
                      </a>
                    </div>
                    <div style={{ marginTop: 10, display: 'flex', flexWrap: 'wrap', gap: 12 }}>
                      <span>过期：{item.expireAt ? new Date(item.expireAt).toLocaleString() : '不限'}</span>
                      <span>
                        下载：{item.downloadCount} / {item.maxDownloads ?? '不限'}
                      </span>
                      <span>剩余：{item.remaining == null ? '不限' : item.remaining}</span>
                      <span>评论：{item.includeComments ? '包含' : '不包含'}</span>
                    </div>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    <button
                      className="button"
                      onClick={() => {
                        navigator.clipboard.writeText(window.location.origin + item.url);
                        showToast('已复制链接到剪贴板', 'success');
                      }}
                    >
                      复制链接
                    </button>
                    <button className="button danger" onClick={() => handleRevoke(item.token)}>
                      撤销
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
