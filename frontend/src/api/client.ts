import axios from 'axios'

const BASE = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '') + '/api/v1'

export const api = axios.create({ baseURL: BASE })

api.interceptors.request.use(cfg => {
  const token = localStorage.getItem('mg_token')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

api.interceptors.response.use(
  r => r,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('mg_token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ─── Auth ───────────────────────────────────────────────
export const login = (username: string, password: string) =>
  api.post('/auth/login', { username, password }).then(r => r.data)

export const register = (data: object) =>
  api.post('/auth/register', data).then(r => r.data)

// ─── Dashboard ─────────────────────────────────────────
export const getDashboardStats = () =>
  api.get('/dashboard/stats').then(r => r.data)

// ─── Patienten ─────────────────────────────────────────
export const getPatienten = () =>
  api.get('/patienten').then(r => r.data)

export const getPatient = (id: number) =>
  api.get(`/patienten/${id}`).then(r => r.data)

export const searchPatienten = (q: string) =>
  api.get('/patienten/suche', { params: { q } }).then(r => r.data)

export const createPatient = (data: object) =>
  api.post('/patienten', data).then(r => r.data)

export const updatePatient = (id: number, data: object) =>
  api.put(`/patienten/${id}`, data).then(r => r.data)

// ─── Krankenakten ───────────────────────────────────────
export const getAktenByPatient = (patientenId: number) =>
  api.get(`/krankenakten/patient/${patientenId}`).then(r => r.data)

export const createAkte = (data: object) =>
  api.post('/krankenakten', data).then(r => r.data)

// ─── Vorfälle ───────────────────────────────────────────
export const getVorfaelle = (status?: string) =>
  api.get('/vorfaelle', { params: status ? { status } : {} }).then(r => r.data)

export const createVorfall = (data: object) =>
  api.post('/vorfaelle', data).then(r => r.data)

export const updateVorfall = (id: number, data: object) =>
  api.patch(`/vorfaelle/${id}`, data).then(r => r.data)

export const getDsgvoPending = () =>
  api.get('/vorfaelle/dsgvo-ausstehend').then(r => r.data)

// ─── Audit ─────────────────────────────────────────────
export const getAuditLog = () =>
  api.get('/audit').then(r => r.data)
