import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Dashboard from './pages/Dashboard';
import Login from './pages/Login';
import Register from './pages/Register';
import Upload from './pages/Upload';
import Share from './pages/Share';
import ShareManager from './pages/ShareManager';
import Preview from './pages/Preview';
import SharedView from './pages/SharedView';

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/upload"
          element={
            <ProtectedRoute>
              <Upload />
            </ProtectedRoute>
          }
        />
        <Route
          path="/share"
          element={
            <ProtectedRoute>
              <Share />
            </ProtectedRoute>
          }
        />
        <Route
          path="/share/manage"
          element={
            <ProtectedRoute>
              <ShareManager />
            </ProtectedRoute>
          }
        />
        <Route
          path="/preview/:id"
          element={
            <ProtectedRoute>
              <Preview />
            </ProtectedRoute>
          }
        />        <Route path="/shared/:token" element={<SharedView />} />        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  );
}
