import React, { useEffect, useState } from 'react';
import api from '../api';
import { useToast } from '../components/ToastContext';

type DocumentItem = {
  id: number;
  filename: string;
  title?: string;
  summary?: string;
};

type Message = {
  id: string;
  type: 'user' | 'ai';
  content: string;
  timestamp: Date;
};

export default function Chat() {
  const [docs, setDocs] = useState<DocumentItem[]>([]);
  const [selectedDocId, setSelectedDocId] = useState<number | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { showToast } = useToast();

  const fetchDocs = async () => {
    setError(null);
    try {
      const resp = await api.get('/documents/list');
      setDocs(resp.data);
    } catch (err: any) {
      setError(err?.response?.data || '获取文档列表失败');
    }
  };

  useEffect(() => {
    fetchDocs();
  }, []);

  const handleSendMessage = async () => {
    if (!inputMessage.trim() || !selectedDocId) {
      showToast('请输入问题并选择文档', 'error');
      return;
    }

    const userMessage: Message = {
      id: Date.now().toString(),
      type: 'user',
      content: inputMessage.trim(),
      timestamp: new Date()
    };

    setMessages(prev => [...prev, userMessage]);
    setInputMessage('');
    setLoading(true);

    try {
      const resp = await api.post(`/chat?doc_id=${selectedDocId}`, {
        question: userMessage.content
      });

      const aiMessage: Message = {
        id: (Date.now() + 1).toString(),
        type: 'ai',
        content: resp.data,
        timestamp: new Date()
      };

      setMessages(prev => [...prev, aiMessage]);
    } catch (err: any) {
      showToast(err?.response?.data || '发送消息失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const clearChat = () => {
    setMessages([]);
  };

  return (
    <div className="container" style={{ maxWidth: 1200 }}>
      <div className="card">
        <h2>AI 智能问答</h2>
        <p>选择一个文档，然后提出您的问题，AI 将基于文档内容为您提供准确的回答。</p>
      </div>

      {error ? <div className="alert error">{error}</div> : null}

      <div style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: 20, height: 'calc(100vh - 200px)' }}>
        {/* 文档选择面板 */}
        <div className="card" style={{ height: 'fit-content' }}>
          <h3>选择文档</h3>
          {docs.length === 0 ? (
            <p>暂无文档，请先上传。</p>
          ) : (
            <div style={{ maxHeight: 400, overflowY: 'auto' }}>
              {docs.map((doc) => (
                <div
                  key={doc.id}
                  className={`card ${selectedDocId === doc.id ? 'primary' : ''}`}
                  style={{
                    margin: '8px 0',
                    cursor: 'pointer',
                    border: selectedDocId === doc.id ? '2px solid var(--color-primary)' : '1px solid var(--color-border)'
                  }}
                  onClick={() => setSelectedDocId(doc.id)}
                >
                  <h4 style={{ margin: 0, fontSize: '0.9rem' }}>{doc.title || doc.filename}</h4>
                  <p style={{ margin: '4px 0 0 0', fontSize: '0.8rem', color: 'rgba(240,246,252,0.7)' }}>
                    ID: {doc.id}
                  </p>
                  {doc.summary && (
                    <p style={{ margin: '4px 0 0 0', fontSize: '0.8rem', color: 'rgba(240,246,252,0.6)' }}>
                      {doc.summary.substring(0, 100)}...
                    </p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 聊天面板 */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <h3>对话</h3>
            <button className="button secondary" onClick={clearChat} disabled={messages.length === 0}>
              清空对话
            </button>
          </div>

          {/* 消息区域 */}
          <div style={{
            flex: 1,
            overflowY: 'auto',
            border: '1px solid var(--color-border)',
            borderRadius: 8,
            padding: 16,
            marginBottom: 16,
            backgroundColor: 'var(--color-bg-secondary)',
            minHeight: 300
          }}>
            {messages.length === 0 ? (
              <div style={{ textAlign: 'center', color: 'rgba(240,246,252,0.5)', marginTop: 100 }}>
                {selectedDocId ? '开始与 AI 对话吧！' : '请先选择一个文档'}
              </div>
            ) : (
              messages.map((message) => (
                <div
                  key={message.id}
                  style={{
                    marginBottom: 16,
                    display: 'flex',
                    justifyContent: message.type === 'user' ? 'flex-end' : 'flex-start'
                  }}
                >
                  <div
                    style={{
                      maxWidth: '70%',
                      padding: 12,
                      borderRadius: 8,
                      backgroundColor: message.type === 'user' ? 'var(--color-primary)' : 'var(--color-bg-tertiary)',
                      color: message.type === 'user' ? 'white' : 'inherit',
                      whiteSpace: 'pre-wrap',
                      wordWrap: 'break-word'
                    }}
                  >
                    {message.content}
                  </div>
                </div>
              ))
            )}
            {loading && (
              <div style={{ textAlign: 'center', color: 'rgba(240,246,252,0.5)' }}>
                AI 正在思考中...
              </div>
            )}
          </div>

          {/* 输入区域 */}
          <div style={{ display: 'flex', gap: 8 }}>
            <textarea
              className="textarea"
              rows={2}
              placeholder={selectedDocId ? "输入您的问题..." : "请先选择文档"}
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              disabled={!selectedDocId || loading}
              style={{ flex: 1 }}
            />
            <button
              className="button primary"
              onClick={handleSendMessage}
              disabled={!inputMessage.trim() || !selectedDocId || loading}
            >
              {loading ? '发送中...' : '发送'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
