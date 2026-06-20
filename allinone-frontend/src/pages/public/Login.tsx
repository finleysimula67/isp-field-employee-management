import { useState, useEffect, useRef } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../../api/auth'
import client from '../../api/client'
import { useAuth } from '../../contexts/AuthContext'
import type { Employee } from '../../types'

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: { client_id: string; callback: (res: { credential: string }) => void }) => void
          renderButton: (el: HTMLElement, options: { theme: string; size: string; text: string; shape: string; width: number }) => void
        }
      }
    }
  }
}

const backendBase =
  import.meta.env.VITE_BACKEND_URL ||
  (import.meta.env.VITE_API_URL || 'https://allinone-backend-xoh0.onrender.com/api')
    .replace(/\/api$/, '')

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [warming, setWarming] = useState(true)
  const [warmFailures, setWarmFailures] = useState(0)
  const [warmExhausted, setWarmExhausted] = useState(false)
  const navigate = useNavigate()
  const { login: authLogin } = useAuth()
  const googleBtnRef = useRef<HTMLDivElement>(null)
  const googleInitRef = useRef(false)

  useEffect(() => {
    let cancelled = false
    let retries = 0
    let timer: ReturnType<typeof setTimeout>
    const MAX_RETRIES = 30
    const ping = async () => {
      if (cancelled) return
      try {
        await fetch(`${backendBase}/api/auth/check-email?email=ping`, { signal: AbortSignal.timeout(15000) })
        if (!cancelled) { setWarming(false); return }
      } catch {
        /* server not ready yet */
      }
      if (!cancelled) {
        retries++
        if (retries >= MAX_RETRIES) {
          setWarming(false); setWarmExhausted(true)
          return
        }
        timer = setTimeout(ping, 2000)
      }
    }
    ping()
    return () => { cancelled = true; clearTimeout(timer) }
  }, [])

  useEffect(() => {
    if (googleInitRef.current || !window.google?.accounts?.id) return
    googleInitRef.current = true
    window.google.accounts.id.initialize({
      client_id: '74808369318-pmgvg483tsmj51m7i2bi6e50fff95kt9.apps.googleusercontent.com',
      callback: async (res) => {
        try {
          setError('')
          const { data } = await client.post('/auth/google-login', { idToken: res.credential })
          if (data.success) {
            const d = data.data
            const empRes = await client.get('/employees/me')
            const employeeData = empRes.data.data as Employee
            authLogin(d.token, employeeData)
            navigate(d.role === 'SUPER_ADMIN' || d.role === 'BRANCH_MANAGER' ? '/admin' : '/employee')
          } else {
            setError(data.message || 'Google login failed')
          }
        } catch (err: any) {
          setError(err.response?.data?.message || 'Google sign-in failed. Please try again.')
        }
      }
    })
    if (googleBtnRef.current) {
      window.google.accounts.id.renderButton(googleBtnRef.current, {
        theme: 'outline', size: 'large', text: 'signin_with', shape: 'rectangular', width: 380
      })
    }
  }, [warming, authLogin, navigate])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const res = await login({ email, password })
      if (res.success) {
        let employeeData: Employee
        try {
          const empRes = await client.get('/employees/me')
          employeeData = empRes.data.data as Employee
        } catch {
          employeeData = {
            id: res.data.userId,
            email: res.data.email,
            name: res.data.name,
            phone: null,
            role: res.data.role as Employee['role'],
            branchId: null, branchName: null, authType: 'LOCAL_ONLY',
            isActive: true, isAccountApproved: true,
            wageType: null, dailyRate: null, hourlyWage: null,
            totalLeaveDaysPerYear: 0, remainingLeaveDays: 0, carryOverLeave: 0, maxAdvanceLimit: 5000, isOwner: false,
            createdAt: new Date().toISOString(),
          }
        }
        authLogin(res.data.token, employeeData)
        if (res.data.role === 'SUPER_ADMIN' || res.data.role === 'BRANCH_MANAGER') {
          navigate('/admin')
        } else {
          navigate('/employee')
        }
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-[70vh] flex items-center justify-center px-5 py-10">
      <div className="w-full max-w-sm">
        <div className="text-center mb-6">
          <div className="w-10 h-10 bg-brand-600 rounded-xl flex items-center justify-center mx-auto mb-3">
            <span className="text-white font-bold text-sm">A</span>
          </div>
          <h1 className="font-display text-xl font-bold text-gray-900">Welcome Back</h1>
          <p className="text-gray-500 text-xs mt-1">Sign in to your account</p>
          {warming && (
            <p className="text-amber-600 text-xs mt-2 flex items-center justify-center gap-1">
              <svg className="w-3.5 h-3.5 animate-spin" fill="none" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
              Waking up server{warmFailures > 0 ? ` (attempt ${warmFailures + 1})` : '...'}
            </p>
          )}
          {warmExhausted && (
            <p className="text-red-600 text-xs mt-2">Server unreachable — please try again later</p>
          )}
        </div>
        <form onSubmit={handleSubmit} className="bg-white rounded-xl p-5 md:p-6 border border-slate-100 shadow-sm space-y-4">
          {error && (
            <div className="bg-red-50 text-red-700 text-sm px-4 py-2 rounded-md">{error}</div>
          )}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="input-field w-full"
              placeholder="you@allinone.com"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="input-field w-full pr-10"
                placeholder="••••••••"
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPassword ? (
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                  </svg>
                ) : (
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                )}
              </button>
            </div>
          </div>
          <div className="text-right">
            <Link to="/forgot-password" className="text-sm text-brand-600 hover:text-brand-700">
              Forgot password?
            </Link>
          </div>
          <button type="submit" disabled={loading || warming} className="btn-primary w-full disabled:opacity-50">
            {warming ? 'Connecting...' : loading ? 'Signing in...' : 'Sign In'}
          </button>
          <div className="relative my-4">
            <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-gray-200" /></div>
            <div className="relative flex justify-center"><span className="bg-white px-3 text-xs text-gray-400">or</span></div>
          </div>
          <div className="flex justify-center w-full">
            <div ref={googleBtnRef} className={warming ? 'pointer-events-none opacity-40' : ''}></div>
          </div>
        </form>
      </div>
    </div>
  )
}
