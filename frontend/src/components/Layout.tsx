import { NavLink, useNavigate } from 'react-router-dom'
import { Shield, LayoutDashboard, Users, FileText, AlertTriangle, ClipboardList, LogOut, ChevronRight } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'

const nav = [
  { to: '/dashboard',  icon: LayoutDashboard, label: 'Dashboard'        },
  { to: '/patienten',  icon: Users,            label: 'Patienten'        },
  { to: '/akten',      icon: FileText,         label: 'Krankenakten'     },
  { to: '/vorfaelle',  icon: AlertTriangle,    label: 'Sicherheitsvorfälle' },
  { to: '/audit',      icon: ClipboardList,    label: 'Audit-Protokoll'  },
]

const ROLE_COLORS: Record<string, string> = {
  ADMIN: '#DC2626', ARZT: '#1C4ED8', PFLEGER: '#16A34A', VERWALTUNG: '#D97706'
}

export default function Layout({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="flex h-screen overflow-hidden">
      {/* Sidebar */}
      <aside className="w-56 flex flex-col shrink-0"
        style={{ background: 'var(--bg-sidebar)', borderRight: '1px solid rgba(255,255,255,0.06)' }}>

        {/* Logo */}
        <div className="px-5 py-5 flex items-center gap-3"
          style={{ borderBottom: '1px solid rgba(255,255,255,0.06)' }}>
          <div className="w-8 h-8 rounded-lg flex items-center justify-center"
            style={{ background: '#1C4ED8' }}>
            <Shield size={16} className="text-white" />
          </div>
          <div>
            <div className="text-sm font-bold text-white" style={{ fontFamily: 'Space Grotesk' }}>MediGuard</div>
            <div className="text-xs" style={{ color: '#475569', fontFamily: 'IBM Plex Mono' }}>Deutschland</div>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 p-3 flex flex-col gap-0.5 overflow-y-auto">
          {nav.map(({ to, icon: Icon, label }) => (
            <NavLink key={to} to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-xs font-medium transition-all ${
                  isActive ? 'text-white' : ''}`}
              style={({ isActive }) => ({
                background: isActive ? 'rgba(28,78,216,0.25)' : 'transparent',
                color: isActive ? 'white' : 'var(--sidebar-text)',
                borderLeft: isActive ? '2px solid #3B82F6' : '2px solid transparent',
              })}>
              <Icon size={15} />{label}
            </NavLink>
          ))}
        </nav>

        {/* DSGVO note */}
        <div className="mx-3 mb-3 p-2.5 rounded-lg text-xs"
          style={{ background: 'rgba(28,78,216,0.12)', color: '#93C5FD', border: '1px solid rgba(59,130,246,0.2)' }}>
          🔒 DSGVO-konform<br />
          <span style={{ color: '#475569' }}>§630f BGB · §33 DSGVO</span>
        </div>

        {/* User */}
        <div className="p-3" style={{ borderTop: '1px solid rgba(255,255,255,0.06)' }}>
          <div className="flex items-center gap-2 px-2 py-2 rounded-lg mb-1"
            style={{ background: 'rgba(255,255,255,0.04)' }}>
            <div className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white"
              style={{ background: ROLE_COLORS[user?.role ?? 'ADMIN'] ?? '#1C4ED8' }}>
              {user?.fullName?.[0] ?? 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-xs font-medium text-white truncate">{user?.fullName}</div>
              <div className="text-xs" style={{ color: '#475569' }}>{user?.role}</div>
            </div>
          </div>
          <button onClick={() => { logout(); navigate('/login') }}
            className="flex items-center gap-2 w-full px-2 py-1.5 rounded text-xs transition-colors"
            style={{ color: '#64748B' }}
            onMouseEnter={e => e.currentTarget.style.color = '#EF4444'}
            onMouseLeave={e => e.currentTarget.style.color = '#64748B'}>
            <LogOut size={13} /> Abmelden
          </button>
        </div>
      </aside>

      {/* Main */}
      <main className="flex-1 overflow-y-auto" style={{ background: 'var(--bg-primary)' }}>
        {children}
      </main>
    </div>
  )
}
