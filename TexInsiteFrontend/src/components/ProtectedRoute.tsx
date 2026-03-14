import React from 'react';
import { Navigate } from 'react-router-dom';

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('texinsite_token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}
