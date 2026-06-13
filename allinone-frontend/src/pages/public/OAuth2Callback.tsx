import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'
import type { Employee } from '../../types'

export default function OAuth2Callback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { login } = useAuth()
  const [status, setStatus] = useState<'loading' | 'pending' | 'error'>('loading')
  const [message, setMessage] = useState('')

  useEffect(() => {
    const error = searchParams.get('error')
    if (error) {
      setStatus('error')
      setMessage(error)
      return
    }

    const pending = searchParams.get('pending')
    if (pending === 'true') {
      setStatus('pending')
      setMessage('Your account is pending admin approval. Please ask your manager.')
      return
    }

    const token = searchParams.get('token')
    const userId = searchParams.get('userId')
    const role = searchParams.get('role')
    const name = searchParams.get('name')
    const email = searchParams.get('email')
    const approved = searchParams.get('approved')

    if (!token || !userId || !role) {
      setStatus('error')
      setMessage('Invalid response from Google. Please try again.')
      return
    }

    const employee: Employee = {
      id: Number(userId),
      email: email || '',
      name: name || '',
      role: role as Employee['role'],
      phone: null,
      branchId: null,
      branchName: null,
      authType: 'GOOGLE_ONLY',
      isActive: true,
      isAccountApproved: approved === 'true',
      wageType: null,
      dailyRate: null,
      hourlyWage: null,
      totalLeaveDaysPerYear: 0,
      remainingLeaveDays: 0,
      carryOverLeave: 0,
      maxAdvanceLimit: 5000,
      isOwner: false,
      createdAt: new Date().toISOString(),
    }

    login(token, employee)

    if (role === 'SUPER_ADMIN' || role === 'BRANCH_MANAGER') {
      navigate('/admin')
    } else {
      navigate('/employee')
    }
  }, [])

  return (
    <div className="min-h-[70vh] flex items-center justify-center px-5 py-10">
      <div className="w-full max-w-sm text-center">
        <div className="bg-white rounded-xl p-6 md:p-8 border border-slate-100 shadow-sm">
          {status === 'loading' && (
            <div>
              <div className="w-10 h-10 bg-brand-600 rounded-xl flex items-center justify-center mx-auto mb-4 animate-pulse">
                <span className="text-white font-bold text-sm">A</span>
              </div>
              <p className="text-sm text-gray-500">Completing sign in...</p>
            </div>
          )}
          {status === 'pending' && (
            <div>
              <div className="w-12 h-12 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl">⏳</span>
              </div>
              <h2 className="font-display text-lg font-bold text-gray-900 mb-2">Pending Approval</h2>
              <p className="text-sm text-gray-500 leading-relaxed mb-4">{message}</p>
              <button onClick={() => navigate('/login')} className="btn-primary w-full text-sm">Back to Login</button>
            </div>
          )}
          {status === 'error' && (
            <div>
              <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl">❌</span>
              </div>
              <h2 className="font-display text-lg font-bold text-gray-900 mb-2">Sign In Failed</h2>
              <p className="text-sm text-gray-500 leading-relaxed mb-4">{message}</p>
              <button onClick={() => navigate('/login')} className="btn-primary w-full text-sm">Back to Login</button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
