import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function Layout({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('texinsite_token');
    localStorage.removeItem('texinsite_username');
    navigate('/login');
  };

  const username = localStorage.getItem('texinsite_username');

  return (
    <div>
      <header className="header">
        <div>
          <Link to="/" style={{ color: 'inherit', textDecoration: 'none' }}>
            <h1>TexInsite</h1>
          </Link>
          <div style={{ fontSize: '0.9rem', color: 'rgba(240,246,252,0.7)' }}>
            安全文档分享 & AI 智能问答
          </div>
        </div>

        <nav style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <Link to="/" className="nav-link">看板</Link>
          <Link to="/upload" className="nav-link">上传</Link>
          <Link to="/chat" className="nav-link">聊天</Link>
          <Link to="/share/manage" className="nav-link">分享管理</Link>
        </nav>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          {username ? (
            <>
              <span style={{ opacity: 0.8 }}>Hi, {username}</span>
              <button className="button" onClick={handleLogout}>
                退出登录
              </button>
            </>
          ) : (
            <Link to="/login" className="button">
              登录 / 注册
            </Link>
          )}
        </div>
      </header>

      <main className="container">{children}</main>

      <footer className="footer">
        TexInsite • 2026 • 基于 Spring Boot + React
      </footer>
    </div>
  );
}
