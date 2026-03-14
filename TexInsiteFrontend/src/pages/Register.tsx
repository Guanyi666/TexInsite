import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api';
import { useToast } from '../components/ToastContext';

export default function Register() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    // If already logged in, redirect to dashboard.
    if (localStorage.getItem('texinsite_token')) {
      navigate('/');
    }
  }, [navigate]);

  const getErrorMessage = (err: any, fallback: string) => {
    if (!err) return fallback;
    const data = err?.response?.data;
    if (typeof data === 'string' && data.trim()) return data.trim();
    if (data?.message && String(data.message).trim()) return String(data.message).trim();
    if (data && typeof data === 'object') {
      const candidate = (data.error || data.errors || data.detail || data.message) as any;
      if (candidate && String(candidate).trim()) return String(candidate).trim();
      try {
        const json = JSON.stringify(data);
        if (json && json !== '{}' && json !== 'null') return json;
      } catch {
        // ignore
      }
    }
    if (err?.message && String(err.message).trim()) return String(err.message).trim();
    return fallback;
  };

  const handleRegister = async () => {
    setError(null);
    setSuccess(null);
    try {
      await api.post('/auth/register', { username, password });
      setSuccess('注册成功，请登录。');
      showToast('注册成功，请登录。', 'success');
      setTimeout(() => navigate('/login'), 1200);
    } catch (err: any) {
      const msg = getErrorMessage(err, '注册失败，请稍后重试');
      setError(msg);
      showToast(msg, 'error');
    }
  };

  return (
    <div className="container" style={{ maxWidth: 420 }}>
      <div className="card">
        <h2>注册</h2>
        <div className="label">用户名</div>
        <input
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          className="input"
          placeholder="请输入用户名"
        />
        <div className="label">密码</div>
        <input
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="input"
          type="password"
          placeholder="请输入密码"
        />

        {error ? <div className="alert error">{error}</div> : null}
        {success ? <div className="alert success">{success}</div> : null}

        <button className="button primary" style={{ marginTop: '1.1rem' }} onClick={handleRegister}>
          注册
        </button>

        <div style={{ marginTop: '1.1rem' }}>
          已有账号？ <Link to="/login">去登录</Link>
        </div>
      </div>
    </div>
  );
}
