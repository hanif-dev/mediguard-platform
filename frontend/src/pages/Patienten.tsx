import { useEffect, useState } from 'react'
import { Users, Plus, Search, X, ChevronRight } from 'lucide-react'
import { getPatienten, searchPatienten, createPatient } from '../api/client'
import { PageHeader, VersicherungBadge, EmptyState, Loader } from '../components/UI'
import toast from 'react-hot-toast'

const GESCHLECHT_OPTIONS = ['MAENNLICH', 'WEIBLICH', 'DIVERS']
const VERSICHERUNG_OPTIONS = ['GESETZLICH', 'PRIVAT', 'BERUFSGENOSSENSCHAFT']

function PatientModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const empty = { nachname: '', vorname: '', geburtsdatum: '', geschlecht: 'MAENNLICH',
    adresse: '', plz: '', stadt: '', telefon: '', email: '', versicherungsart: 'GESETZLICH',
    versicherungsnummer: '', krankenkasse: '', allergien: '', vorerkrankungen: '', behandelnderArzt: '' }
  const [form, setForm] = useState(empty)
  const [loading, setLoading] = useState(false)
  const s = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.nachname || !form.vorname || !form.geburtsdatum) {
      toast.error('Pflichtfelder ausfüllen'); return
    }
    setLoading(true)
    try {
      await createPatient(form)
      toast.success('Patient erfolgreich angelegt')
      onCreated(); onClose()
    } catch (err: any) {
      toast.error(err?.response?.data?.detail ?? 'Fehler beim Anlegen')
    } finally { setLoading(false) }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ background: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(4px)' }}>
      <div className="card w-full max-w-2xl max-h-[90vh] overflow-y-auto fade-up">
        <div className="flex items-center justify-between px-5 py-4 border-b" style={{ borderColor: 'var(--border)' }}>
          <span className="font-semibold">Neuer Patient anlegen</span>
          <button onClick={onClose}><X size={16} style={{ color: 'var(--text-muted)' }} /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-5 form-section">
          <div className="form-row">
            <div className="form-group"><label>Nachname *</label>
              <input className="input" value={form.nachname} onChange={e => s('nachname', e.target.value)} placeholder="Mustermann" /></div>
            <div className="form-group"><label>Vorname *</label>
              <input className="input" value={form.vorname} onChange={e => s('vorname', e.target.value)} placeholder="Max" /></div>
          </div>
          <div className="form-row">
            <div className="form-group"><label>Geburtsdatum *</label>
              <input className="input" type="date" value={form.geburtsdatum} onChange={e => s('geburtsdatum', e.target.value)} /></div>
            <div className="form-group"><label>Geschlecht</label>
              <select className="input" value={form.geschlecht} onChange={e => s('geschlecht', e.target.value)}>
                {GESCHLECHT_OPTIONS.map(o => <option key={o}>{o}</option>)}</select></div>
          </div>
          <div className="form-group"><label>Adresse</label>
            <input className="input" value={form.adresse} onChange={e => s('adresse', e.target.value)} placeholder="Hauptstraße 1" /></div>
          <div className="form-row">
            <div className="form-group"><label>PLZ</label>
              <input className="input" value={form.plz} onChange={e => s('plz', e.target.value)} placeholder="10115" /></div>
            <div className="form-group"><label>Stadt</label>
              <input className="input" value={form.stadt} onChange={e => s('stadt', e.target.value)} placeholder="Berlin" /></div>
          </div>
          <div className="form-row">
            <div className="form-group"><label>Telefon</label>
              <input className="input" value={form.telefon} onChange={e => s('telefon', e.target.value)} /></div>
            <div className="form-group"><label>E-Mail</label>
              <input className="input" type="email" value={form.email} onChange={e => s('email', e.target.value)} /></div>
          </div>
          <div className="form-row">
            <div className="form-group"><label>Versicherungsart</label>
              <select className="input" value={form.versicherungsart} onChange={e => s('versicherungsart', e.target.value)}>
                {VERSICHERUNG_OPTIONS.map(o => <option key={o}>{o}</option>)}</select></div>
            <div className="form-group"><label>Krankenkasse</label>
              <input className="input" value={form.krankenkasse} onChange={e => s('krankenkasse', e.target.value)} placeholder="AOK, TK, DAK..." /></div>
          </div>
          <div className="form-group"><label>Versicherungsnummer</label>
            <input className="input" value={form.versicherungsnummer} onChange={e => s('versicherungsnummer', e.target.value)} /></div>
          <div className="form-group"><label>Allergien</label>
            <textarea className="input" value={form.allergien} onChange={e => s('allergien', e.target.value)} placeholder="z.B. Penicillin, Latex..." /></div>
          <div className="form-group"><label>Vorerkrankungen</label>
            <textarea className="input" value={form.vorerkrankungen} onChange={e => s('vorerkrankungen', e.target.value)} /></div>
          <div className="form-group"><label>Behandelnder Arzt</label>
            <input className="input" value={form.behandelnderArzt} onChange={e => s('behandelnderArzt', e.target.value)} /></div>
          <div className="flex gap-2 pt-1">
            <button type="button" onClick={onClose} className="btn btn-secondary flex-1 justify-center">Abbrechen</button>
            <button type="submit" className="btn btn-primary flex-1 justify-center" disabled={loading}>
              {loading ? <span className="w-4 h-4 rounded-full border-2 border-white/30 spin" style={{ borderTopColor: 'white' }} /> : 'Anlegen'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function Patienten() {
  const [patients, setPatients] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [showModal, setShowModal] = useState(false)

  const load = () => {
    setLoading(true)
    const fn = query.trim() ? searchPatienten(query) : getPatienten()
    fn.then(setPatients).catch(console.error).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])
  useEffect(() => {
    const t = setTimeout(() => load(), 350)
    return () => clearTimeout(t)
  }, [query])

  return (
    <div className="p-6 fade-up">
      <PageHeader title="Patienten" subtitle="Patientenverwaltung — DSGVO-konform"
        action={<button className="btn btn-primary" onClick={() => setShowModal(true)}><Plus size={14} />Neuer Patient</button>} />

      <div className="card mb-4 p-3 flex items-center gap-3">
        <Search size={15} style={{ color: 'var(--text-muted)' }} />
        <input className="flex-1 outline-none text-sm bg-transparent" placeholder="Patient suchen (Name)..."
          value={query} onChange={e => setQuery(e.target.value)} style={{ color: 'var(--text-primary)' }} />
        {query && <button onClick={() => setQuery('')}><X size={13} style={{ color: 'var(--text-muted)' }} /></button>}
      </div>

      <div className="card overflow-hidden">
        {loading ? <Loader /> : patients.length === 0 ? (
          <EmptyState icon={<Users size={32} />} text="Keine Patienten gefunden" />
        ) : (
          <table className="tbl">
            <thead><tr>
              <th>Pat.-Nr.</th><th>Name</th><th>Geburtsdatum</th>
              <th>Versicherung</th><th>Krankenkasse</th><th>Arzt</th><th></th>
            </tr></thead>
            <tbody>
              {patients.map((p: any) => (
                <tr key={p.id}>
                  <td className="mono">{p.patientenNr}</td>
                  <td><span className="font-medium">{p.nachname}, {p.vorname}</span></td>
                  <td>{p.geburtsdatum}</td>
                  <td><VersicherungBadge art={p.versicherungsart} /></td>
                  <td>{p.krankenkasse ?? '—'}</td>
                  <td style={{ color: 'var(--text-secondary)' }}>{p.behandelnderArzt ?? '—'}</td>
                  <td><ChevronRight size={14} style={{ color: 'var(--text-muted)' }} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
      {showModal && <PatientModal onClose={() => setShowModal(false)} onCreated={load} />}
    </div>
  )
}
