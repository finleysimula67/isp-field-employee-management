import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../../api/auth'
import client from '../../api/client'
import { useAuth } from '../../contexts/AuthContext'
import type { Employee } from '../../types'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { login: authLogin } = useAuth()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const res = await login({ email, password })
      if (res.success) {
        const minimalEmployee: Employee = {
          id: res.data.userId,
          email: res.data.email,
          name: res.data.name,
          role: res.data.role as Employee['role'],
          branchId: null, branchName: null, authType: 'LOCAL_ONLY',
          isActive: true, isAccountApproved: true,
          wageType: null, dailyRate: null, hourlyWage: null,
          totalLeaveDaysPerYear: 0, remainingLeaveDays: 0, carryOverLeave: 0, maxAdvanceLimit: 5000, isOwner: false,
          createdAt: new Date().toISOString(),
        }
        authLogin(res.data.token, minimalEmployee)
        try {
          const empRes = await client.get(`/employees/${res.data.userId}`)
          authLogin(res.data.token, empRes.data.data as Employee)
        } catch {
          // full details fetch failed — minimal employee already stored above
        }
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
              className="input-field"
              placeholder="you@allinone.com"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="input-field"
              placeholder="••••••••"
              required
            />
          </div>
          <div className="text-right">
            <Link to="/forgot-password" className="text-sm text-brand-600 hover:text-brand-700">
              Forgot password?
            </Link>
          </div>
          <button type="submit" disabled={loading} className="btn-primary w-full disabled:opacity-50">
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
          <div className="relative my-4">
            <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-gray-200" /></div>
            <div className="relative flex justify-center"><span className="bg-white px-3 text-xs text-gray-400">or</span></div>
          </div>
          <button
           type="button"
           onClick={() => window.location.href = 'https://allinone-backend-xoh0.onrender.com/oauth2/authorization/google'}
           className="flex items-center justify-center gap-2 w-full border border-gray-200 rounded-xl py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 hover:border-gray-300 transition-colors"
           >
            <svg className="w-5 h-5" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
            Continue with Google
          </button>
        </form>
      </div>
    </div>
  )
}
//production update
