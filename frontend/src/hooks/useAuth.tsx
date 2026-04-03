import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { login as apiLogin } from '../api/client'
import axios from 'axios'

interface User {
  id: number; username: string; email: string
  fullName: string; role: string; abteilung: string
}
interface AuthCtx {
  user: User | null; token: string | null
  login: (u: string, p: string) => Promise<void>
  logout: () => void; loading: boolean
}

const Ctx = createContext<AuthCtx | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const stored = localStorage.getItem('mg_token')
    const storedUser = localStorage.getItem('mg_user')
    if (stored && storedUser) {
      setToken(stored)
      setUser(JSON.parse(storedUser))
    }
    setLoading(false)
  }, [])

  const login = async (username: string, password: string) => {
    const data = await apiLogin(username, password)
    localStorage.setItem('mg_token', data.token)
    localStorage.setItem('mg_user', JSON.stringify(data.user))
    setToken(data.token)
    setUser(data.user)
  }

  const logout = () => {
    localStorage.removeItem('mg_token')
    localStorage.removeItem('mg_user')
    setToken(null); setUser(null)
  }

  return <Ctx.Provider value={{ user, token, login, logout, loading }}>{children}</Ctx.Provider>
}

export const useAuth = () => {
  const c = useContext(Ctx)
  if (!c) throw new Error('useAuth must be inside AuthProvider')
  return c
}
