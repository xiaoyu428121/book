import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Login from '@/pages/Login'
import Diagnostic from '@/pages/Diagnostic'
import Path from '@/pages/Path'
import Dialogue from '@/pages/Dialogue'
import Shadowing from '@/pages/Shadowing'
import Profile from '@/pages/Profile'
import { useAuthStore } from '@/store/userStore'

// 认证保护组件
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  
  return <>{children}</>
}

// 首页重定向组件
function HomeRedirect() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const user = useAuthStore((state) => state.user)
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  
  if (user && !user.hasCompletedDiagnostic) {
    return <Navigate to="/diagnostic" replace />
  }
  
  return <Navigate to="/path" replace />
}

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomeRedirect />} />
        <Route path="/login" element={<Login />} />
        <Route 
          path="/diagnostic" 
          element={
            <ProtectedRoute>
              <Diagnostic />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/path" 
          element={
            <ProtectedRoute>
              <Path />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/practice/dialogue" 
          element={
            <ProtectedRoute>
              <Dialogue />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/practice/shadowing" 
          element={
            <ProtectedRoute>
              <Shadowing />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/profile" 
          element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          } 
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  )
}