import { useEffect, useState } from 'react'
import { FileText, Plus, X, Lock } from 'lucide-react'
import { getPatienten, getAktenByPatient, createAkte } from '../api/client'
import { PageHeader, EmptyState, Loader } from '../components/UI'
import toast from 'react-hot-toast'

const EINTRAGS_TYPEN = ['ANAMNESE','DIAGNOSE','BEHANDLUNG','MEDIKATION','LABOR','BILDGEBUNG','ENTLASSUNG','NOTIZ']
const TYPE_COLORS: Record<string, string> = {
  ANAMNESE: '#1C4ED8', DIAGNOSE: '#DC2626', BEHANDLUNG: '#16A34A',
  MEDIKATION: '#D97706', LABOR: '#7C3AED', BILDGEBUNG: '#0891B2',
  ENTLASSUNG: '#475569', NOTIZ: '#6B7280'
}

function AkteModal({ patients, onClose, onCreated }: { patients: any[]; onClose: () => void; onCreated: () => void }) {
  const [form, setForm] = useState({
    patientenId: '', eintragsTyp: 'DIAGNOSE', titel: '',
    inhalt: '', icd10Code: '', behandelnderArzt: '', abteilung: '', vertraulich: false
  })
  const [loading, setLoading] = useState(false)
  const s = (k: string, v: any) => setForm(f => ({ ...f, [k]: v }))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.patientenId || !form.titel || !form.inhalt || !form.behandelnderArzt) {
      toast.error('Pflichtfelder ausfüllen'); return
    }
    setLoading(true)
    try {
      await createAkte({ ...form, patientenId: Number(form.patientenId) })
      toast.success('Eintrag gespeichert')
      onCreated(); onClose()
    } catch (err: any) {
      toast.error(err?.response?.data?.detail ?? 'Fehler')
    } finally { setLoading(false) }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ background: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(4px)' }}>
      <div className="card w-full max-w-xl max-h-[90vh] overflow-y-auto fade-up">
        <div className="flex items-center justify-between px-5 py-4 border-b" style={{ borderColor: 'var(--border)' }}>
          <span className="font-semibold">Neuer Akten-Eintrag (§630f BGB)</span>
          <button onClick={onClose}><X size={16} style={{ color: 'var(--text-muted)' }} /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-5 form-section">
          <div className="form-group"><label>Patient *</label>
            <select className="input" value={form.patientenId} onChange={e => s('patientenId', e.target.value)}>
              <option value="">— Patient auswählen —</option>
              {patients.map((p: any) => <option key={p.id} value={p.id}>{p.patientenNr} — {p.nachname}, {p.vorname}</option>)}
            </select></div>
          <div className="form-row">
            <div className="form-group"><label>Eintragstyp *</label>
              <select className="input" value={form.eintragsTyp} onChange={e => s('eintragsTyp', e.target.value)}>
                {EINTRAGS_TYPEN.map(t => <option key={t}>{t}</option>)}</select></div>
            <div className="form-group"><label>ICD-10 Code</label>
              <input className="input" value={form.icd10Code} onChange={e => s('icd10Code', e.target.value)} placeholder="z.B. E11.9" /></div>
          </div>
          <div className="form-group"><label>Titel *</label>
            <input className="input" value={form.titel} onChange={e => s('titel', e.target.value)} placeholder="Kurzbeschreibung des Eintrags" /></div>
          <div className="form-group"><label>Inhalt *</label>
            <textarea className="input" rows={5} value={form.inhalt} onChange={e => s('inhalt', e.target.value)} placeholder="Detaillierter Befund / Behandlungsverlauf..." /></div>
          <div className="form-row">
            <div className="form-group"><label>Behandelnder Arzt *</label>
              <input className="input" value={form.behandelnderArzt} onChange={e => s('behandelnderArzt', e.target.value)} /></div>
            <div className="form-group"><label>Abteilung</label>
              <input className="input" value={form.abteilung} onChange={e => s('abteilung', e.target.value)} /></div>
          </div>
          <label className="flex items-center gap-2 cursor-pointer text-sm">
            <input type="checkbox" checked={form.vertraulich} onChange={e => s('vertraulich', e.target.checked)} />
            <Lock size={13} style={{ color: 'var(--text-muted)' }} />
            Vertraulich (nur für behandelnden Arzt)
          </label>
          <div className="flex gap-2 pt-1">
            <button type="button" onClick={onClose} className="btn btn-secondary flex-1 justify-center">Abbrechen</button>
            <button type="submit" className="btn btn-primary flex-1 justify-center" disabled={loading}>Speichern</button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function Krankenakten() {
  const [patients, setPatients] = useState<any[]>([])
  const [selectedPatient, setSelectedPatient] = useState<number | null>(null)
  const [akten, setAkten] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)

  useEffect(() => { getPatienten().then(setPatients).catch(console.error) }, [])

  const loadAkten = (id: number) => {
    setSelectedPatient(id); setLoading(true)
    getAktenByPatient(id).then(setAkten).catch(console.error).finally(() => setLoading(false))
  }

  const selectedP = patients.find(p => p.id === selectedPatient)

  return (
    <div className="p-6 fade-up">
      <PageHeader title="Krankenakten" subtitle="§630f BGB — Unveränderliche Dokumentation"
        action={<button className="btn btn-primary" onClick={() => setShowModal(true)}><Plus size={14} />Neuer Eintrag</button>} />

      <div className="grid grid-cols-1 xl:grid-cols-4 gap-4">
        {/* Patient list */}
        <div className="card overflow-hidden">
          <div className="px-4 py-3 border-b text-xs font-semibold uppercase tracking-wide"
            style={{ borderColor: 'var(--border)', color: 'var(--text-secondary)', background: 'var(--bg-primary)' }}>
            Patient auswählen
          </div>
          <div className="overflow-y-auto" style={{ maxHeight: 500 }}>
            {patients.map((p: any) => (
              <button key={p.id} onClick={() => loadAkten(p.id)}
                className="w-full text-left px-4 py-3 border-b text-sm transition-colors"
                style={{
                  borderColor: 'var(--border)',
                  background: selectedPatient === p.id ? 'var(--accent-light)' : 'white',
                  borderLeft: selectedPatient === p.id ? '3px solid var(--accent)' : '3px solid transparent',
                }}>
                <div className="font-medium">{p.nachname}, {p.vorname}</div>
                <div className="text-xs mt-0.5" style={{ color: 'var(--text-muted)', fontFamily: 'IBM Plex Mono' }}>{p.patientenNr}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Akten */}
        <div className="card xl:col-span-3 overflow-hidden">
          {!selectedPatient ? (
            <EmptyState icon={<FileText size={32} />} text="Patient auswählen, um Akten anzuzeigen" />
          ) : loading ? <Loader /> : (
            <>
              {selectedP && (
                <div className="px-5 py-4 border-b flex items-center gap-3" style={{ borderColor: 'var(--border)' }}>
                  <div>
                    <div className="font-semibold">{selectedP.nachname}, {selectedP.vorname}</div>
                    <div className="text-xs" style={{ color: 'var(--text-muted)' }}>{selectedP.patientenNr} · geb. {selectedP.geburtsdatum}</div>
                  </div>
                </div>
              )}
              {akten.length === 0 ? (
                <EmptyState icon={<FileText size={28} />} text="Noch keine Einträge vorhanden" />
              ) : (
                <div className="divide-y" style={{ borderColor: 'var(--border)' }}>
                  {akten.map((a: any) => (
                    <div key={a.id} className="p-5">
                      <div className="flex items-start justify-between gap-3 mb-2">
                        <div className="flex items-center gap-2">
                          <span className="badge text-white" style={{ background: TYPE_COLORS[a.eintragsTyp] ?? '#475569', border: 'none' }}>
                            {a.eintragsTyp}
                          </span>
                          {a.icd10Code && <span className="mono text-xs px-2 py-0.5 rounded" style={{ background: 'var(--bg-primary)', color: 'var(--text-secondary)' }}>{a.icd10Code}</span>}
                          {a.vertraulich && <Lock size={12} style={{ color: 'var(--text-muted)' }} />}
                        </div>
                        <span className="text-xs" style={{ color: 'var(--text-muted)', fontFamily: 'IBM Plex Mono', whiteSpace: 'nowrap' }}>
                          {new Date(a.erstelltAm).toLocaleString('de-DE')}
                        </span>
                      </div>
                      <div className="font-semibold mb-1">{a.titel}</div>
                      <div className="text-sm leading-relaxed mb-2" style={{ color: 'var(--text-secondary)' }}>{a.inhalt}</div>
                      <div className="text-xs" style={{ color: 'var(--text-muted)' }}>
                        Dr. {a.behandelnderArzt} {a.abteilung && `· ${a.abteilung}`} · Erstellt von: {a.erstelltVon}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      </div>
      {showModal && <AkteModal patients={patients} onClose={() => setShowModal(false)} onCreated={() => selectedPatient && loadAkten(selectedPatient)} />}
    </div>
  )
}
