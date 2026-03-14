import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

export default function Upload() {
  const navigate = useNavigate();
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    setError(null);
    setSuccess(null);
    if (!file) {
      setError('请先选择一个文件');
      return;
    }

    setLoading(true);
    try {
      const form = new FormData();
      form.append('file', file);
      await api.post('/documents/upload', form, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      setSuccess('上传成功，稍等片刻即可在列表中看到文档');
      setTimeout(() => {
        navigate('/');
      }, 1200);
    } catch (err: any) {
      setError(err?.response?.data || '上传失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: 620 }}>
      <div className="card">
        <h2>上传文档</h2>
        <div className="label">请选择 PDF 文件</div>
        <input
          type="file"
          accept="application/pdf"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
          className="input"
        />

        {error ? <div className="alert error">{error}</div> : null}
        {success ? <div className="alert success">{success}</div> : null}

        <button className="button primary" onClick={handleSubmit} disabled={loading}>
          {loading ? '上传中...' : '上传'}
        </button>
      </div>
    </div>
  );
}
