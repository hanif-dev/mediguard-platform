import { useEffect, useState } from 'react'
import { ClipboardList } from 'lucide-react'
import { getAuditLog } from '../api/client'
import { PageHeader, EmptyState, Loader } from '../components/UI'

const ACTION_COLORS: Record<string, string> = {
  LOGIN: '#16A34A', LOGOUT: '#6B7280',
  PATIENT_CREATE: '#1C4ED8', PATIENT_UPDATE: '#D97706', PATIENT_DELETE: '#DC2626',
  RECORD_VIEW: '#7C3AED', RECORD_CREATE: '#1C4ED8',
}

export default function AuditProtokoll() {
  const [logs, setLogs] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getAuditLog().then(setLogs).catch(console.error).finally(() => setLoading(false))
  }, [])

  return (
    <div className="p-6 fade-up">
      <PageHeader title="Audit-Protokoll" subtitle="DSGVO-Konformitätsprotokoll — alle Systemzugriffe (nur Admin)" />
      <div className="card p-3 mb-4 text-xs" style={{ background: 'var(--amber-light)', border: '1px solid #FCD34D', color: 'var(--amber)' }}>
        🔒 Dieses Protokoll ist unveränderlich und dient dem Nachweis der DSGVO-Konformität.
        Alle Zugriffe auf Patientendaten werden automatisch erfasst.
      </div>
      <div className="card overflow-hidden">
        {loading ? <Loader /> : logs.length === 0 ? (
          <EmptyState icon={<ClipboardList size={32} />} text="Noch keine Einträge" />
        ) : (
          <table className="tbl">
            <thead><tr>
              <th>Zeitstempel</th><th>Benutzer</th><th>Aktion</th>
              <th>Ressource</th><th>Details</th><th>Status</th>
            </tr></thead>
            <tbody>
              {logs.map((l: any) => (
                <tr key={l.id}>
                  <td className="mono text-xs" style={{ color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                    {new Date(l.zeitstempel).toLocaleString('de-DE')}
                  </td>
                  <td className="font-medium">{l.benutzerName}</td>
                  <td>
                    <span className="badge text-white text-xs"
                      style={{ background: ACTION_COLORS[l.aktion] ?? '#475569', border: 'none' }}>
                      {l.aktion}
                    </span>
                  </td>
                  <td className="text-xs" style={{ color: 'var(--text-secondary)' }}>
                    {l.ressourceTyp ?? '—'} {l.ressourceId ? `#${l.ressourceId}` : ''}
                  </td>
                  <td className="text-xs max-w-xs truncate" style={{ color: 'var(--text-secondary)' }}
                    title={l.details}>{l.details ?? '—'}</td>
                  <td>
                    {l.warErfolgreich
                      ? <span className="badge badge-geloest">OK</span>
                      : <span className="badge badge-hoch">FEHLER</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
