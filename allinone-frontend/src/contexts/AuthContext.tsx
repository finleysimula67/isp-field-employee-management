import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react'
import type { Employee } from '../types'

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    if (payload.exp) return Date.now() >= payload.exp * 1000
  } catch {}
  return false
}

interface AuthContextType {
  user: Employee | null
  token: string | null
  isAuthenticated: boolean
  login: (token: string, user: Employee) => void
  logout: () => void
  hasRole: (...roles: string[]) => boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Employee | null>(() => {
    try {
      const saved = localStorage.getItem('user')
      return saved ? JSON.parse(saved) : null
    } catch { return null }
  })
  const [token, setToken] = useState<string | null>(() => {
    const t = localStorage.getItem('token')
    if (t && isTokenExpired(t)) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      return null
    }
    return t
  })

  useEffect(() => {
    if (!token) return
    const interval = setInterval(() => {
      if (isTokenExpired(token)) {
        logout()
      }
    }, 60_000)
    return () => clearInterval(interval)
  }, [token])

  const loginFn = (t: string, u: Employee) => {
    setToken(t)
    setUser(u)
    localStorage.setItem('token', t)
    localStorage.setItem('user', JSON.stringify(u))
  }

  const logout = () => {
    setToken(null)
    setUser(null)
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    window.location.href = '/login'
  }

  const hasRole = (...roles: string[]) => {
    if (!user) return false
    return roles.includes(user.role)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        login: loginFn,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
