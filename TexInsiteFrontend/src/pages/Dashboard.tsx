import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api';

type DocumentItem = {
  id: number;
  filename: string;
  title?: string;
  summary?: string;
  uploadedAt?: string;
  author?: string;
  deletedAt?: string;
};

type ShareLinkItem = {
  token: string;
  url: string;
  expireAt: string | null;
  maxDownloads: number | null;
  downloadCount: number;
  remaining: number | null;
  documentId: number;
  documentTitle: string | null;
  documentFilename: string;
};

function formatDateLabel(date: Date) {
  return `${date.getMonth() + 1}/${date.getDate()}`;
}

export default function Dashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'overview' | 'documents' | 'shares' | 'trash'>('overview');
  const [docs, setDocs] = useState<DocumentItem[]>([]);
  const [trashDocs, setTrashDocs] = useState<DocumentItem[]>([]);
  const [shareLinks, setShareLinks] = useState<ShareLinkItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingDoc, setEditingDoc] = useState<DocumentItem | null>(null);
  const [editTitle, setEditTitle] = useState('');
  const [editAuthor, setEditAuthor] = useState('');

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [docsResp, shareResp, trashResp] = await Promise.all([
        api.get('/documents/list'),
        api.get('/share/list'),
        api.get('/documents/trash')
      ]);
      const docsData: DocumentItem[] = docsResp.data || [];
      const shareData: ShareLinkItem[] = shareResp.data || [];
      const trashData: DocumentItem[] = trashResp.data || [];
      setDocs(docsData);
      setShareLinks(shareData);
      setTrashDocs(trashData);
    } catch (err: any) {
      if (err?.response?.status === 401) {
        navigate('/login');
        return;
      }
      setError(err?.response?.data || '获取看板数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleEditDoc = (doc: DocumentItem) => {
    setEditingDoc(doc);
    setEditTitle(doc.title || '');
    setEditAuthor(doc.author || '');
  };

  const handleSaveEdit = async () => {
    if (!editingDoc) return;
    try {
      await api.put(`/documents/${editingDoc.id}`, {
        title: editTitle.trim() || null,
        author: editAuthor.trim() || null
      });
      setEditingDoc(null);
      fetchData();
    } catch (err: any) {
      alert('更新失败: ' + (err?.response?.data || err.message));
    }
  };

  const handleDeleteDoc = async (docId: number) => {
    if (!confirm('确定要删除此文档吗？它将被移至回收站7天后自动删除。')) return;
    try {
      await api.delete(`/documents/${docId}`);
      fetchData();
    } catch (err: any) {
      alert('删除失败: ' + (err?.response?.data || err.message));
    }
  };

  const handleRestoreDoc = async (docId: number) => {
    try {
      await api.post(`/documents/${docId}/restore`);
      fetchData();
    } catch (err: any) {
      alert('恢复失败: ' + (err?.response?.data || err.message));
    }
  };

  const handlePermanentDelete = async (docId: number) => {
    if (!confirm('确定要彻底删除此文档吗？此操作不可撤销。')) return;
    try {
      await api.delete(`/documents/${docId}/permanent`);
      fetchData();
    } catch (err: any) {
      alert('彻底删除失败: ' + (err?.response?.data || err.message));
    }
  };

  const handleRevokeLink = async (token: string) => {
    try {
      await api.delete(`/share/${token}`);
      fetchData();
    } catch (err) {
      // ignore
      console.error(err);
    }
  };

  const stats = useMemo(() => {
    const docsCount = docs.length;
    const trashCount = trashDocs.length;
    const shareCount = shareLinks.length;

    // 计算总下载数
    const totalDownloads = shareLinks.reduce((sum, link) => sum + link.downloadCount, 0);

    // 计算活跃分享（未过期）
    const activeShares = shareLinks.filter(link => !link.expireAt || new Date(link.expireAt) > new Date()).length;

    // 计算过期分享
    const expiredShares = shareCount - activeShares;

    const now = new Date();
    const last7Days = Array.from({ length: 7 }).map((_, idx) => {
      const d = new Date(now);
      d.setDate(now.getDate() - (6 - idx));
      const dayKey = d.toISOString().slice(0, 10);
      return { label: formatDateLabel(d), dayKey, uploadCount: 0, deleteCount: 0 };
    });

    const byDay = new Map(last7Days.map((item) => [item.dayKey, item]));
    docs.forEach((doc) => {
      if (!doc.uploadedAt) return;
      const key = new Date(doc.uploadedAt).toISOString().slice(0, 10);
      const entry = byDay.get(key);
      if (entry) entry.uploadCount += 1;
    });

    trashDocs.forEach((doc) => {
      if (!doc.deletedAt) return;
      const key = new Date(doc.deletedAt).toISOString().slice(0, 10);
      const entry = byDay.get(key);
      if (entry) entry.deleteCount += 1;
    });

    const maxCount = Math.max(1, ...last7Days.map((d) => d.uploadCount + d.deleteCount));
    return {
      docsCount,
      trashCount,
      shareCount,
      totalDownloads,
      activeShares,
      expiredShares,
      uploadTrend: last7Days,
      maxCount
    };
  }, [docs, shareLinks, trashDocs]);

  const renderTrendChart = () => {
    const barMaxHeight = 100;
    const barWidth = 20;
    return (
      <div style={{ display: 'flex', alignItems: 'flex-end', gap: 15, padding: '1rem 0' }}>
        {stats.uploadTrend.map((day) => {
          const uploadHeight = Math.round((day.uploadCount / stats.maxCount) * barMaxHeight);
          const deleteHeight = Math.round((day.deleteCount / stats.maxCount) * barMaxHeight);
          return (
            <div key={day.dayKey} style={{ textAlign: 'center', flex: 1, minWidth: 50 }}>
              <div style={{ display: 'flex', justifyContent: 'center', gap: 2, alignItems: 'flex-end' }}>
                <div
                  style={{
                    height: Math.max(uploadHeight, 4),
                    width: barWidth,
                    background: 'rgba(84, 200, 255, 0.8)',
                    borderRadius: 4,
                    transition: 'height 0.2s ease',
                    position: 'relative'
                  }}
                  title={`上传: ${day.uploadCount}`}
                >
                  <div style={{
                    position: 'absolute',
                    top: -20,
                    left: '50%',
                    transform: 'translateX(-50%)',
                    fontSize: '0.7rem',
                    color: 'var(--accent)',
                    whiteSpace: 'nowrap'
                  }}>
                    {day.uploadCount}
                  </div>
                </div>
                <div
                  style={{
                    height: Math.max(deleteHeight, 4),
                    width: barWidth,
                    background: 'rgba(255, 92, 92, 0.8)',
                    borderRadius: 4,
                    transition: 'height 0.2s ease',
                    position: 'relative'
                  }}
                  title={`删除: ${day.deleteCount}`}
                >
                  <div style={{
                    position: 'absolute',
                    top: -20,
                    left: '50%',
                    transform: 'translateX(-50%)',
                    fontSize: '0.7rem',
                    color: 'var(--danger)',
                    whiteSpace: 'nowrap'
                  }}>
                    {day.deleteCount}
                  </div>
                </div>
              </div>
              <div style={{ marginTop: 25, fontSize: '0.75rem', color: 'rgba(240,246,252,0.7)' }}>{day.label}</div>
            </div>
          );
        })}
      </div>
    );
  };

  const renderOverview = () => (
    <>
      <div className="card">
        <h2>看板概览</h2>
        <p>这里展示系统统计信息及最近一周的上传趋势。</p>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginTop: 16 }}>
          <div style={{ background: 'linear-gradient(135deg, rgba(84, 200, 255, 0.2), rgba(84, 200, 255, 0.1))', border: '1px solid rgba(84, 200, 255, 0.3)', borderRadius: 12, padding: '1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>📄</div>
            <div style={{ fontSize: '0.85rem', color: 'rgba(240,246,252,0.7)' }}>总文档数</div>
            <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--accent)' }}>{stats.docsCount}</div>
          </div>
          <div style={{ background: 'linear-gradient(135deg, rgba(255, 92, 92, 0.2), rgba(255, 92, 92, 0.1))', border: '1px solid rgba(255, 92, 92, 0.3)', borderRadius: 12, padding: '1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>🗑️</div>
            <div style={{ fontSize: '0.85rem', color: 'rgba(240,246,252,0.7)' }}>删除文档数</div>
            <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--danger)' }}>{stats.trashCount}</div>
          </div>
          <div style={{ background: 'linear-gradient(135deg, rgba(34, 197, 94, 0.2), rgba(34, 197, 94, 0.1))', border: '1px solid rgba(34, 197, 94, 0.3)', borderRadius: 12, padding: '1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>🔗</div>
            <div style={{ fontSize: '0.85rem', color: 'rgba(240,246,252,0.7)' }}>分享链接数</div>
            <div style={{ fontSize: '2rem', fontWeight: 700, color: '#22c55e' }}>{stats.shareCount}</div>
          </div>
          <div style={{ background: 'linear-gradient(135deg, rgba(168, 85, 247, 0.2), rgba(168, 85, 247, 0.1))', border: '1px solid rgba(168, 85, 247, 0.3)', borderRadius: 12, padding: '1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>⬇️</div>
            <div style={{ fontSize: '0.85rem', color: 'rgba(240,246,252,0.7)' }}>总下载次数</div>
            <div style={{ fontSize: '2rem', fontWeight: 700, color: '#a855f7' }}>{stats.totalDownloads}</div>
          </div>
          <div style={{ background: 'linear-gradient(135deg, rgba(251, 191, 36, 0.2), rgba(251, 191, 36, 0.1))', border: '1px solid rgba(251, 191, 36, 0.3)', borderRadius: 12, padding: '1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>✅</div>
            <div style={{ fontSize: '0.85rem', color: 'rgba(240,246,252,0.7)' }}>活跃分享</div>
            <div style={{ fontSize: '2rem', fontWeight: 700, color: '#fbbf24' }}>{stats.activeShares}</div>
          </div>
          <div style={{ background: 'linear-gradient(135deg, rgba(156, 163, 175, 0.2), rgba(156, 163, 175, 0.1))', border: '1px solid rgba(156, 163, 175, 0.3)', borderRadius: 12, padding: '1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>⏰</div>
            <div style={{ fontSize: '0.85rem', color: 'rgba(240,246,252,0.7)' }}>过期分享</div>
            <div style={{ fontSize: '2rem', fontWeight: 700, color: '#9ca3af' }}>{stats.expiredShares}</div>
          </div>
        </div>

        <div style={{ marginTop: 24 }}>
          <div style={{ fontSize: '0.9rem', color: 'rgba(240,246,252,0.7)', marginBottom: '0.5rem' }}>最近 7 天趋势</div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', fontSize: '0.8rem', marginBottom: '0.5rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
              <div style={{ width: 12, height: 12, background: 'rgba(84, 200, 255, 0.8)', borderRadius: 2 }}></div>
              <span style={{ color: 'rgba(240,246,252,0.7)' }}>上传</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
              <div style={{ width: 12, height: 12, background: 'rgba(255, 92, 92, 0.8)', borderRadius: 2 }}></div>
              <span style={{ color: 'rgba(240,246,252,0.7)' }}>删除</span>
            </div>
          </div>
          {renderTrendChart()}
        </div>

        <div style={{ marginTop: 20, display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <Link to="/upload" className="button primary">
            上传文档
          </Link>
          <Link to="/share/manage" className="button">
            管理分享链接
          </Link>
        </div>
      </div>
    </>
  );

  const renderDocuments = () => (
    <div className="stack">
      {docs.length === 0 ? (
        <div className="card">您还没有上传任何文档，先去上传一个吧。</div>
      ) : (
        docs.map((doc) => (
          <div key={doc.id} className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ fontSize: '1.2rem' }}>📄</span>
                <div>
                  <h3 style={{ margin: 0 }}>{doc.title || doc.filename}</h3>
                  {doc.author && <div style={{ fontSize: '0.9rem', color: 'rgba(240,246,252,0.7)' }}>作者: {doc.author}</div>}
                </div>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button className="button" onClick={() => handleEditDoc(doc)}>
                  编辑
                </button>
                <button className="button" onClick={() => navigate(`/share?docId=${doc.id}`)}>
                  生成分享
                </button>
                <button className="button" onClick={() => navigate(`/preview/${doc.id}`)}>
                  预览
                </button>
                <button className="button danger" onClick={() => handleDeleteDoc(doc.id)}>
                  删除
                </button>
              </div>
            </div>

            {doc.summary ? (
              <p style={{ marginTop: '1rem', color: 'rgba(240,246,252,0.75)' }}>{doc.summary}</p>
            ) : null}
            <div style={{ fontSize: '0.9rem', color: 'rgba(240,246,252,0.7)', marginTop: 6 }}>
              上传时间: {doc.uploadedAt ? new Date(doc.uploadedAt).toLocaleString() : '-'}
            </div>
          </div>
        ))
      )}
    </div>
  );

  const renderTrash = () => (
    <div className="stack">
      {trashDocs.length === 0 ? (
        <div className="card">回收站是空的。</div>
      ) : (
        trashDocs.map((doc) => {
          const deletedAt = new Date(doc.deletedAt!);
          const now = new Date();
          const daysLeft = Math.max(0, 7 - Math.floor((now.getTime() - deletedAt.getTime()) / (1000 * 60 * 60 * 24)));
          return (
            <div key={doc.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                <div>
                  <h3 style={{ margin: 0 }}>{doc.title || doc.filename}</h3>
                  {doc.author && <div style={{ fontSize: '0.9rem', color: 'rgba(240,246,252,0.7)' }}>作者: {doc.author}</div>}
                  <div style={{ fontSize: '0.9rem', color: 'rgba(240,246,252,0.7)', marginTop: 6 }}>
                    删除时间: {deletedAt.toLocaleString()}
                  </div>
                  <div style={{ fontSize: '0.9rem', color: 'rgba(255,92,92,0.8)', marginTop: 4 }}>
                    距彻底删除还剩: {daysLeft} 天
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="button primary" onClick={() => handleRestoreDoc(doc.id)}>
                    恢复
                  </button>
                  <button className="button danger" onClick={() => handlePermanentDelete(doc.id)}>
                    彻底删除
                  </button>
                </div>
              </div>
            </div>
          );
        })
      )}
    </div>
  );

  const renderShares = () => (
    <div className="stack">
      {shareLinks.length === 0 ? (
        <div className="card">暂无分享链接，先去生成一个。</div>
      ) : (
        shareLinks.map((item) => (
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
                </div>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                <button
                  className="button"
                  onClick={() => {
                    navigator.clipboard.writeText(window.location.origin + item.url);
                  }}
                >
                  复制链接
                </button>
                <button className="button danger" onClick={() => handleRevokeLink(item.token)}>
                  撤销
                </button>
              </div>
            </div>
          </div>
        ))
      )}
    </div>
  );

  return (
    <div>
      <div className="card" style={{ marginBottom: 12 }}>
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <button
            className="button"
            style={
              activeTab === 'overview'
                ? { background: 'rgba(84, 200, 255, 0.25)', borderColor: 'rgba(84, 200, 255, 0.4)' }
                : undefined
            }
            onClick={() => setActiveTab('overview')}
          >
            概览
          </button>
          <button
            className="button"
            style={
              activeTab === 'documents'
                ? { background: 'rgba(84, 200, 255, 0.25)', borderColor: 'rgba(84, 200, 255, 0.4)' }
                : undefined
            }
            onClick={() => setActiveTab('documents')}
          >
            文档
          </button>
          <button
            className="button"
            style={
              activeTab === 'shares'
                ? { background: 'rgba(84, 200, 255, 0.25)', borderColor: 'rgba(84, 200, 255, 0.4)' }
                : undefined
            }
            onClick={() => setActiveTab('shares')}
          >
            分享
          </button>
          <button
            className="button"
            style={
              activeTab === 'trash'
                ? { background: 'rgba(84, 200, 255, 0.25)', borderColor: 'rgba(84, 200, 255, 0.4)' }
                : undefined
            }
            onClick={() => setActiveTab('trash')}
          >
            回收站
          </button>
        </div>
      </div>

      {editingDoc && (
        <div className="card" style={{ marginBottom: 12 }}>
          <h3>编辑文档信息</h3>
          <div className="label">标题</div>
          <input
            value={editTitle}
            onChange={(e) => setEditTitle(e.target.value)}
            className="input"
            placeholder="文档标题"
          />
          <div className="label">作者</div>
          <input
            value={editAuthor}
            onChange={(e) => setEditAuthor(e.target.value)}
            className="input"
            placeholder="文档作者"
          />
          <div style={{ marginTop: 12, display: 'flex', gap: 12 }}>
            <button className="button primary" onClick={handleSaveEdit}>
              保存
            </button>
            <button className="button" onClick={() => setEditingDoc(null)}>
              取消
            </button>
          </div>
        </div>
      )}

      {error ? <div className="alert error">{error}</div> : null}
      {loading ? (
        <div className="card">加载中...</div>
      ) : activeTab === 'overview' ? (
        renderOverview()
      ) : activeTab === 'documents' ? (
        renderDocuments()
      ) : activeTab === 'shares' ? (
        renderShares()
      ) : (
        renderTrash()
      )}
    </div>
  );
}
