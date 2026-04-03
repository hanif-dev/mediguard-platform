import { useEffect, useState } from 'react'
import { AlertTriangle, Plus, X, ChevronDown } from 'lucide-react'
import { getVorfaelle, createVorfall, updateVorfall } from '../api/client'
import { PageHeader, SchwereradBadge, StatusBadge, EmptyState, Loader } from '../components/UI'
import toast from 'react-hot-toast'

const KATEGORIEN = ['DATENPANNE','PHISHING','RANSOMWARE','UNBEFUGTER_ZUGRIFF','GERAETEVERLUST','SOCIAL_ENGINEERING','INSIDER_BEDROHUNG','SONSTIGES']
const SCHWEREGRADE = ['NIEDRIG','MITTEL','HOCH','KRITISCH']
const STATUSLISTE = ['OFFEN','IN_BEARBEITUNG','GELOEST','GESCHLOSSEN']

function VorfallModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const [form, setForm] = useState({
    titel: '', beschreibung: '', kategorie: 'PHISHING', schweregrad: 'MITTEL',
    betroffeneSysteme: '', betroffenePatienten: '', dsgvoMeldepflichtig: false,
    melderName: '', melderAbteilung: ''
  })
  const [loading, setLoading] = useState(false)
  const s = (k: string, v: any) => setForm(f => ({ ...f, [k]: v }))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.titel || !form.beschreibung || !form.melderName) {
      toast.error('Pflichtfelder ausfüllen'); return
    }
    setLoading(true)
    try {
      await createVorfall({ ...form, betroffenePatienten: form.betroffenePatienten ? Number(form.betroffenePatienten) : null })
      toast.success('Vorfall gemeldet')
      onCreated(); onClose()
    } catch { toast.error('Fehler beim Speichern') }
    finally { setLoading(false) }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ background: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(4px)' }}>
      <div className="card w-full max-w-lg max-h-[90vh] overflow-y-auto fade-up">
        <div className="flex items-center justify-between px-5 py-4 border-b" style={{ borderColor: 'var(--border)' }}>
          <span className="font-semibold">Vorfall melden</span>
          <button onClick={onClose}><X size={16} style={{ color: 'var(--text-muted)' }} /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-5 form-section">
          <div className="form-group"><label>Titel *</label>
            <input className="input" value={form.titel} onChange={e => s('titel', e.target.value)} /></div>
          <div className="form-group"><label>Beschreibung *</label>
            <textarea className="input" rows={3} value={form.beschreibung} onChange={e => s('beschreibung', e.target.value)} /></div>
          <div className="form-row">
            <div className="form-group"><label>Kategorie</label>
              <select className="input" value={form.kategorie} onChange={e => s('kategorie', e.target.value)}>
                {KATEGORIEN.map(k => <option key={k}>{k.replace(/_/g,' ')}</option>)}</select></div>
            <div className="form-group"><label>Schweregrad</label>
              <select className="input" value={form.schweregrad} onChange={e => s('schweregrad', e.target.value)}>
                {SCHWEREGRADE.map(k => <option key={k}>{k}</option>)}</select></div>
          </div>
          <div className="form-group"><label>Betroffene Systeme</label>
            <input className="input" value={form.betroffeneSysteme} onChange={e => s('betroffeneSysteme', e.target.value)} placeholder="z.B. KIS, E-Mail-Server, Workstations" /></div>
          <div className="form-row">
            <div className="form-group"><label>Betr. Patienten (Anzahl)</label>
              <input className="input" type="number" value={form.betroffenePatienten} onChange={e => s('betroffenePatienten', e.target.value)} /></div>
            <div className="form-group">
              <label style={{ marginBottom: 20 }}>DSGVO §33 Meldepflicht</label>
              <label className="flex items-center gap-2 cursor-pointer text-sm mt-1">
                <input type="checkbox" checked={form.dsgvoMeldepflichtig} onChange={e => s('dsgvoMeldepflichtig', e.target.checked)} />
                BSI-Meldung erforderlich (72h-Frist)
              </label>
            </div>
          </div>
          <div className="form-row">
            <div className="form-group"><label>Melder *</label>
              <input className="input" value={form.melderName} onChange={e => s('melderName', e.target.value)} /></div>
            <div className="form-group"><label>Abteilung</label>
              <input className="input" value={form.melderAbteilung} onChange={e => s('melderAbteilung', e.target.value)} /></div>
          </div>
          <div className="flex gap-2 pt-1">
            <button type="button" onClick={onClose} className="btn btn-secondary flex-1 justify-center">Abbrechen</button>
            <button type="submit" className="btn btn-primary flex-1 justify-center" disabled={loading}>Melden</button>
          </div>
        </form>
      </div>
    </div>
  )
}

function VorfallRow({ v, onUpdate }: { v: any; onUpdate: () => void }) {
  const [expanded, setExpanded] = useState(false)
  const [updating, setUpdating] = useState(false)

  const nextStatus: Record<string, string> = { OFFEN: 'IN_BEARBEITUNG', IN_BEARBEITUNG: 'GELOEST', GELOEST: 'GESCHLOSSEN' }
  const nextLabel: Record<string, string> = { OFFEN: 'Bearbeitung starten', IN_BEARBEITUNG: 'Als gelöst markieren', GELOEST: 'Schließen' }

  const handleUpdate = async () => {
    if (!nextStatus[v.status]) return
    setUpdating(true)
    try {
      await updateVorfall(v.id, { status: nextStatus[v.status] })
      toast.success('Status aktualisiert'); onUpdate()
    } catch { toast.error('Fehler') }
    finally { setUpdating(false) }
  }

  return (
    <>
      <tr className="cursor-pointer hover:bg-gray-50 transition-colors" onClick={() => setExpanded(e => !e)}
        style={{ borderBottom: '1px solid var(--border)' }}>
        <td className="px-4 py-3 mono text-xs" style={{ color: 'var(--text-muted)' }}>#{v.id}</td>
        <td className="px-4 py-3">
          <div className="font-medium text-sm">{v.titel}</div>
          {v.dsgvoMeldepflichtig && !v.bsiGemeldet && (
            <span className="badge badge-dsgvo mt-1">DSGVO §33 AUSSTEHEND</span>
          )}
        </td>
        <td className="px-4 py-3 text-xs" style={{ color: 'var(--text-secondary)' }}>{v.kategorie?.replace(/_/g,' ')}</td>
        <td className="px-4 py-3"><SchwereradBadge level={v.schweregrad} /></td>
        <td className="px-4 py-3"><StatusBadge status={v.status} /></td>
        <td className="px-4 py-3 text-xs" style={{ color: 'var(--text-secondary)' }}>{v.melderName}</td>
        <td className="px-4 py-3 text-xs mono" style={{ color: 'var(--text-muted)' }}>
          {new Date(v.gemeldetAm).toLocaleDateString('de-DE')}
        </td>
        <td className="px-4 py-3">
          <ChevronDown size={14} className={`transition-transform ${expanded ? 'rotate-180' : ''}`} style={{ color: 'var(--text-muted)' }} />
        </td>
      </tr>
      {expanded && (
        <tr style={{ background: '#F8FAFC', borderBottom: '1px solid var(--border)' }}>
          <td colSpan={8} className="px-5 py-4">
            <div className="grid grid-cols-2 gap-4 text-sm mb-3">
              <div>
                <div className="text-xs font-semibold uppercase tracking-wide mb-1" style={{ color: 'var(--text-muted)' }}>Beschreibung</div>
                <div style={{ color: 'var(--text-primary)' }}>{v.beschreibung}</div>
              </div>
              {v.betroffeneSysteme && (
                <div>
                  <div className="text-xs font-semibold uppercase tracking-wide mb-1" style={{ color: 'var(--text-muted)' }}>Betroffene Systeme</div>
                  <div style={{ color: 'var(--text-primary)' }}>{v.betroffeneSysteme}</div>
                </div>
              )}
            </div>
            <div className="flex items-center gap-3">
              {nextStatus[v.status] && (
                <button className="btn btn-primary text-xs py-1.5 px-3" onClick={e => { e.stopPropagation(); handleUpdate() }} disabled={updating}>
                  {updating ? '...' : nextLabel[v.status]}
                </button>
              )}
              {v.dsgvoMeldepflichtig && !v.bsiGemeldet && (
                <button className="btn text-xs py-1.5 px-3"
                  style={{ background: '#4C1D95', color: 'white' }}
                  onClick={e => { e.stopPropagation(); updateVorfall(v.id, { bsiGemeldet: true }).then(() => { toast.success('BSI-Meldung bestätigt'); onUpdate() }) }}>
                  BSI-Meldung bestätigen
                </button>
              )}
            </div>
          </td>
        </tr>
      )}
    </>
  )
}

export default function Vorfaelle() {
  const [vorfaelle, setVorfaelle] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all')
  const [showModal, setShowModal] = useState(false)

  const load = () => {
    setLoading(true)
    getVorfaelle(filter === 'all' ? undefined : filter)
      .then(setVorfaelle).catch(console.error).finally(() => setLoading(false))
  }
  useEffect(() => { load() }, [filter])

  return (
    <div className="p-6 fade-up">
      <PageHeader title="Sicherheitsvorfälle" subtitle="BSI IT-Grundschutz · §33 DSGVO Meldepflicht"
        action={<button className="btn btn-primary" onClick={() => setShowModal(true)}><Plus size={14} />Vorfall melden</button>} />

      <div className="flex gap-2 mb-4 flex-wrap">
        {['all', ...STATUSLISTE].map(s => (
          <button key={s} onClick={() => setFilter(s)}
            className="btn text-xs py-1.5 px-3"
            style={{
              background: filter === s ? 'var(--accent)' : 'white',
              color: filter === s ? 'white' : 'var(--text-secondary)',
              border: `1px solid ${filter === s ? 'transparent' : 'var(--border)'}`,
            }}>
            {s === 'all' ? 'Alle' : s.replace('_', ' ')}
          </button>
        ))}
      </div>

      <div className="card overflow-hidden">
        {loading ? <Loader /> : vorfaelle.length === 0 ? (
          <EmptyState icon={<AlertTriangle size={32} />} text="Keine Vorfälle vorhanden" />
        ) : (
          <table className="tbl">
            <thead><tr>
              <th>#</th><th>Titel</th><th>Kategorie</th><th>Schweregrad</th>
              <th>Status</th><th>Melder</th><th>Datum</th><th></th>
            </tr></thead>
            <tbody>
              {vorfaelle.map((v: any) => <VorfallRow key={v.id} v={v} onUpdate={load} />)}
            </tbody>
          </table>
        )}
      </div>
      {showModal && <VorfallModal onClose={() => setShowModal(false)} onCreated={load} />}
    </div>
  )
}
