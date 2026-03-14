import React, { useEffect, useMemo, useState } from 'react';
import api from '../api';
import { useToast } from '../components/ToastContext';

type DocumentItem = {
  id: number;
  filename: string;
  title?: string;
  summary?: string;
};

export default function MultiChat() {
  const [docs, setDocs] = useState<DocumentItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [questions, setQuestions] = useState<Record<number, string>>({});
  const [requesting, setRequesting] = useState<Record<number, boolean>>({});
  const { showToast } = useToast();

  const fetchDocs = async () => {
    setLoading(true);
    setError(null);
    try {
      const resp = await api.get('/documents/list');
      setDocs(resp.data);
    } catch (err: any) {
      setError(err?.response?.data || '获取文档列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocs();
  }, []);

  const handleAsk = async (docId: number) => {
    const question = questions[docId];
    if (!question || !question.trim()) {
      showToast('请输入问题', 'error');
      return;
    }

    setRequesting((prev) => ({ ...prev, [docId]: true }));
    try {
      const resp = await api.post(`/chat?doc_id=${docId}`, { question });
      setAnswers((prev) => ({ ...prev, [docId]: resp.data }));
    } catch (err: any) {
      showToast(err?.response?.data || '提问失败', 'error');
    } finally {
      setRequesting((prev) => ({ ...prev, [docId]: false }));
    }
  };

  const canAsk = useMemo(
    () => (docId: number) => !!questions[docId] && !!questions[docId].trim(),
    [questions]
  );

  return (
    <div>
      <div className="card">
        <h2>多文档问答</h2>
        <p>在列表中选中文档，逐个提问，快速获取每个文档的智能答复。</p>
      </div>

      {error ? <div className="alert error">{error}</div> : null}

      {loading ? (
        <div className="card">加载中...</div>
      ) : (
        <div className="stack">
          {docs.length === 0 ? (
            <div className="card">暂无文档，请先上传。</div>
          ) : (
            docs.map((doc) => (
              <div key={doc.id} className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <h3 style={{ margin: 0 }}>{doc.title || doc.filename}</h3>
                    <div style={{ fontSize: '0.85rem', color: 'rgba(240,246,252,0.7)' }}>
                      文档ID: {doc.id}
                    </div>
                  </div>
                </div>

                <div style={{ marginTop: 14 }}>
                  <textarea
                    className="textarea"
                    rows={3}
                    placeholder="输入你的问题，比如：这篇文档主要讲什么？"
                    value={questions[doc.id] || ''}
                    onChange={(e) => setQuestions((prev) => ({ ...prev, [doc.id]: e.target.value }))}
                  />
                </div>

                <div style={{ display: 'flex', gap: 10, marginTop: 10, flexWrap: 'wrap' }}>
                  <button
                    className="button primary"
                    disabled={!canAsk(doc.id) || requesting[doc.id]}
                    onClick={() => handleAsk(doc.id)}
                  >
                    {requesting[doc.id] ? '请求中…' : '提问'}
                  </button>
                </div>

                {answers[doc.id] ? (
                  <div className="card" style={{ marginTop: 12 }}>
                    <h4>回答</h4>
                    <div style={{ whiteSpace: 'pre-wrap', color: 'rgba(240,246,252,0.9)', lineHeight: 1.6 }}>
                      {answers[doc.id]}
                    </div>
                  </div>
                ) : null}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
