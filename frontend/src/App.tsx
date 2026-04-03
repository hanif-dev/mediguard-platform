import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { AuthProvider, useAuth } from './hooks/useAuth'
import Layout from './components/Layout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Patienten from './pages/Patienten'
import Krankenakten from './pages/Krankenakten'
import Vorfaelle from './pages/Vorfaelle'
import AuditProtokoll from './pages/AuditProtokoll'

function Guard({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth()
  if (loading) return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="w-6 h-6 rounded-full border-2 border-transparent spin" style={{ borderTopColor: 'var(--accent)' }} />
    </div>
  )
  return user ? <>{children}</> : <Navigate to="/login" replace />
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      {[
        ['/dashboard',  <Dashboard />],
        ['/patienten',  <Patienten />],
        ['/akten',      <Krankenakten />],
        ['/vorfaelle',  <Vorfaelle />],
        ['/audit',      <AuditProtokoll />],
      ].map(([path, el]) => (
        <Route key={path as string} path={path as string} element={
          <Guard><Layout>{el as React.ReactNode}</Layout></Guard>
        } />
      ))}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
        <Toaster position="bottom-right" toastOptions={{
          style: { background: 'white', color: '#0F172A', border: '1px solid #E2E6ED', fontSize: '13px' },
          success: { iconTheme: { primary: '#16A34A', secondary: 'white' } },
          error: { iconTheme: { primary: '#DC2626', secondary: 'white' } },
        }} />
      </BrowserRouter>
    </AuthProvider>
  )
}
