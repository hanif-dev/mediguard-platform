import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Shield, Eye, EyeOff, Info } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import toast from 'react-hot-toast'

const DEMO_USERS = [
  { label: 'Admin', user: 'admin', pass: 'MediGuard2024!', color: '#DC2626' },
  { label: 'Arzt', user: 'dr.mueller', pass: 'Arzt2024!', color: '#1C4ED8' },
  { label: 'Pfleger', user: 'pfleger.schmidt', pass: 'Pflege2024!', color: '#16A34A' },
  { label: 'Verwaltung', user: 'verwaltung', pass: 'Verwalt2024!', color: '#D97706' },
]

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      await login(username, password)
      navigate('/dashboard')
    } catch (err: any) {
      toast.error(err?.response?.data?.detail ?? 'Anmeldung fehlgeschlagen')
    } finally { setLoading(false) }
  }

  const quickLogin = (u: string, p: string) => { setUsername(u); setPassword(p) }

  return (
    <div className="min-h-screen flex" style={{ background: 'var(--bg-primary)' }}>
      {/* Left panel */}
      <div className="hidden lg:flex flex-col justify-between w-5/12 p-12"
        style={{ background: 'var(--bg-sidebar)', borderRight: '1px solid rgba(255,255,255,0.06)' }}>
        <div>
          <div className="flex items-center gap-3 mb-12">
            <div className="w-10 h-10 rounded-xl flex items-center justify-center" style={{ background: '#1C4ED8' }}>
              <Shield size={22} className="text-white" />
            </div>
            <div>
              <div className="text-lg font-bold text-white" style={{ fontFamily: 'Space Grotesk' }}>MediGuard</div>
              <div className="text-xs" style={{ color: '#475569', fontFamily: 'IBM Plex Mono' }}>Deutschland GmbH</div>
            </div>
          </div>
          <h1 className="text-4xl font-bold leading-tight mb-4 text-white" style={{ fontFamily: 'Space Grotesk' }}>
            Sicheres<br /><span style={{ color: '#3B82F6' }}>Gesundheitsportal</span>
          </h1>
          <p className="text-sm leading-relaxed" style={{ color: '#64748B' }}>
            DSGVO-konformes Patientenverwaltungssystem für das deutsche Gesundheitswesen.
            Zertifiziert nach §630f BGB und §33 DSGVO.
          </p>
        </div>

        {/* Compliance badges */}
        <div className="grid grid-cols-2 gap-3">
          {[
            ['🔒', 'DSGVO', 'Art. 5, 25, 32'],
            ['📋', '§630f BGB', 'Dokumentationspflicht'],
            ['🏥', 'BSI', 'IT-Grundschutz'],
            ['⚕️', 'KHZG', 'Krankenhaus-IT'],
          ].map(([icon, title, sub]) => (
            <div key={title} className="card p-3" style={{ background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.06)' }}>
              <div className="text-lg mb-1">{icon}</div>
              <div className="text-xs font-semibold text-white">{title}</div>
              <div className="text-xs" style={{ color: '#475569' }}>{sub}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Right panel */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="w-full max-w-sm fade-up">
          <div className="mb-7">
            <h2 className="text-2xl font-bold mb-1" style={{ fontFamily: 'Space Grotesk' }}>Anmelden</h2>
            <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>Zugang zum Klinikportal</p>
          </div>

          {/* Demo credentials */}
          <div className="card p-3 mb-5" style={{ borderColor: '#BFDBFE', background: '#EFF6FF' }}>
            <div className="flex items-center gap-2 mb-2">
              <Info size={13} style={{ color: '#1C4ED8' }} />
              <span className="text-xs font-semibold" style={{ color: '#1C4ED8' }}>Demo-Zugänge</span>
            </div>
            <div className="grid grid-cols-2 gap-1.5">
              {DEMO_USERS.map(d => (
                <button key={d.user} onClick={() => quickLogin(d.user, d.pass)}
                  className="text-left p-2 rounded-md text-xs transition-colors"
                  style={{ background: 'white', border: `1px solid ${d.color}30` }}
                  onMouseEnter={e => e.currentTarget.style.borderColor = d.color}
                  onMouseLeave={e => e.currentTarget.style.borderColor = `${d.color}30`}>
                  <span className="font-semibold" style={{ color: d.color }}>{d.label}</span>
                  <br /><span style={{ color: 'var(--text-secondary)' }}>{d.user}</span>
                </button>
              ))}
            </div>
          </div>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="form-group">
              <label>Benutzername</label>
              <input className="input" placeholder="benutzername" value={username}
                onChange={e => setUsername(e.target.value)} autoComplete="username" />
            </div>
            <div className="form-group">
              <label>Passwort</label>
              <div className="relative">
                <input className="input pr-10" type={showPw ? 'text' : 'password'}
                  placeholder="••••••••" value={password}
                  onChange={e => setPassword(e.target.value)} autoComplete="current-password" />
                <button type="button" onClick={() => setShowPw(p => !p)}
                  className="absolute right-3 top-1/2 -translate-y-1/2"
                  style={{ color: 'var(--text-muted)' }}>
                  {showPw ? <EyeOff size={14} /> : <Eye size={14} />}
                </button>
              </div>
            </div>
            <button type="submit" className="btn btn-primary w-full justify-center" disabled={loading || !username || !password}>
              {loading ? <span className="w-4 h-4 rounded-full border-2 border-white/30 spin" style={{ borderTopColor: 'white' }} /> : 'Anmelden'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
