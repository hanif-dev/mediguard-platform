import { useEffect, useState } from 'react'
import { getDashboardStats, getVorfaelle } from '../api/client'
import { Users, FileText, AlertTriangle, Shield, Clock, CheckCircle } from 'lucide-react'
import { StatCard, PageHeader, Loader, SchwereradBadge, StatusBadge } from '../components/UI'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts'

export default function Dashboard() {
  const [stats, setStats] = useState<any>(null)
  const [vorfaelle, setVorfaelle] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([getDashboardStats(), getVorfaelle()])
      .then(([s, v]) => { setStats(s); setVorfaelle(v.slice(0, 6)) })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="p-6"><Loader /></div>

  const chartData = [
    { name: 'Patienten', value: stats?.totalPatienten ?? 0, color: '#1C4ED8' },
    { name: 'Akten', value: stats?.totalAkten ?? 0, color: '#7C3AED' },
    { name: 'Offen', value: stats?.offeneVorfaelle ?? 0, color: '#DC2626' },
    { name: 'In Bearb.', value: stats?.inBearbeitungVorfaelle ?? 0, color: '#D97706' },
    { name: 'Gelöst', value: (stats?.gesamtVorfaelle ?? 0) - (stats?.offeneVorfaelle ?? 0) - (stats?.inBearbeitungVorfaelle ?? 0), color: '#16A34A' },
  ]

  return (
    <div className="p-6 fade-up">
      <PageHeader title="Dashboard" subtitle="Systemübersicht — MediGuard Deutschland" />

      {/* DSGVO alert */}
      {stats?.dsgvoAusstehend > 0 && (
        <div className="card p-4 mb-5 flex items-center gap-3"
          style={{ background: '#4C1D95', border: '1px solid #6D28D9' }}>
          <AlertTriangle size={18} style={{ color: '#EDE9FE' }} />
          <div>
            <span className="text-sm font-semibold text-white">
              DSGVO-Meldepflicht: {stats.dsgvoAusstehend} Vorfall{stats.dsgvoAusstehend > 1 ? 'fälle' : ''} müssen dem BSI gemeldet werden (72-Stunden-Frist, §33 DSGVO)
            </span>
          </div>
        </div>
      )}

      {/* Stat cards */}
      <div className="grid grid-cols-2 xl:grid-cols-3 gap-4 mb-6">
        <StatCard label="Aktive Patienten" value={stats?.totalPatienten ?? 0}
          icon={<Users size={16} />} sub="Gesamt registriert" color="#1C4ED8" />
        <StatCard label="Krankenakten" value={stats?.totalAkten ?? 0}
          icon={<FileText size={16} />} sub="§630f BGB konform" color="#7C3AED" />
        <StatCard label="Offene Vorfälle" value={stats?.offeneVorfaelle ?? 0}
          icon={<AlertTriangle size={16} />} sub="Erfordern Maßnahmen" color="#DC2626" />
        <StatCard label="Kritische Vorfälle" value={stats?.kritischeVorfaelle ?? 0}
          icon={<Shield size={16} />} sub="Höchste Priorität" color="#DC2626" />
        <StatCard label="In Bearbeitung" value={stats?.inBearbeitungVorfaelle ?? 0}
          icon={<Clock size={16} />} sub="Aktiv untersucht" color="#D97706" />
        <StatCard label="DSGVO ausstehend" value={stats?.dsgvoAusstehend ?? 0}
          icon={<CheckCircle size={16} />} sub="BSI-Meldung offen" color="#7C3AED" />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-5 gap-4">
        {/* Chart */}
        <div className="card p-5 xl:col-span-2">
          <div className="text-sm font-semibold mb-4" style={{ color: 'var(--text-primary)' }}>Systemübersicht</div>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={chartData} barSize={28}>
              <XAxis dataKey="name" tick={{ fontSize: 11, fill: 'var(--text-secondary)' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 11, fill: 'var(--text-secondary)' }} axisLine={false} tickLine={false} allowDecimals={false} />
              <Tooltip contentStyle={{ background: 'white', border: '1px solid var(--border)', borderRadius: 8, fontSize: 12 }} />
              <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                {chartData.map((d, i) => <Cell key={i} fill={d.color} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Recent incidents */}
        <div className="card xl:col-span-3 overflow-hidden">
          <div className="px-5 py-4 border-b flex items-center gap-2" style={{ borderColor: 'var(--border)' }}>
            <AlertTriangle size={14} style={{ color: 'var(--red)' }} />
            <span className="text-sm font-semibold">Neueste Sicherheitsvorfälle</span>
          </div>
          {vorfaelle.length === 0 ? (
            <div className="p-8 text-center text-sm" style={{ color: 'var(--text-muted)' }}>
              Keine Vorfälle vorhanden
            </div>
          ) : (
            <table className="tbl">
              <thead><tr>
                <th>Titel</th><th>Kategorie</th><th>Schweregrad</th><th>Status</th>
              </tr></thead>
              <tbody>
                {vorfaelle.map((v: any) => (
                  <tr key={v.id}>
                    <td className="font-medium" style={{ maxWidth: 200 }}>
                      <div className="truncate">{v.titel}</div>
                    </td>
                    <td><span className="mono text-xs">{v.kategorie?.replace(/_/g, ' ')}</span></td>
                    <td><SchwereradBadge level={v.schweregrad} /></td>
                    <td><StatusBadge status={v.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  )
}
