import { useEffect, useState } from 'react'
import { getProfile, updateProfile, changePassword } from '../../api/profile'
import { getEmployeeStats } from '../../api/dashboard'
import { enableMfa, disableMfa } from '../../api/auth'
import { useAuth } from '../../contexts/AuthContext'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

export default function EmpProfilePage() {
  const { user } = useAuth()
  const [profile, setProfile] = useState<any>(null)
  const [stats, setStats] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState({ name: '' })
  const [passForm, setPassForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [saving, setSaving] = useState(false)
  const [changingPass, setChangingPass] = useState(false)
  const [showCurrentPass, setShowCurrentPass] = useState(false)
  const [showNewPass, setShowNewPass] = useState(false)
  const [showConfirmPass, setShowConfirmPass] = useState(false)
  const [mfaToggling, setMfaToggling] = useState(false)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  useEffect(() => {
    Promise.all([getProfile(), getEmployeeStats()])
      .then(([profRes, statRes]) => {
        const p = profRes.data || profRes
        setProfile(p)
        setForm({ name: p.name || '' })
        setStats(statRes.data)
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [])

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setToast(null)
    try {
      await updateProfile({ name: form.name })
      setToast({ message: 'Profile updated', type: 'success' })
    } catch { setToast({ message: 'Failed to update profile', type: 'error' }) }
    finally { setSaving(false) }
  }

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault()
    if (passForm.newPassword !== passForm.confirmPassword) { setToast({ message: 'Passwords do not match', type: 'error' }); return }
    setChangingPass(true)
    setToast(null)
    try {
      await changePassword({ currentPassword: passForm.currentPassword, newPassword: passForm.newPassword })
      setToast({ message: 'Password changed', type: 'success' })
      setPassForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    } catch { setToast({ message: 'Failed to change password', type: 'error' }) }
    finally { setChangingPass(false) }
  }

  const handleToggleMfa = async () => {
    setMfaToggling(true)
    setToast(null)
    try {
      if (profile.mfaEnabled) {
        await disableMfa()
        setProfile((p: any) => ({ ...p, mfaEnabled: false }))
        setToast({ message: 'MFA disabled', type: 'success' })
      } else {
        await enableMfa()
        setProfile((p: any) => ({ ...p, mfaEnabled: true }))
        setToast({ message: 'MFA enabled — you will be prompted for a code on next login', type: 'success' })
      }
    } catch { setToast({ message: 'Failed to update MFA setting', type: 'error' }) }
    finally { setMfaToggling(false) }
  }

  if (loading) {
    return (
      <div>
        <h1 className="font-display text-xl font-bold text-gray-900 mb-1">Profile</h1>
        <Skeleton variant="card" count={3} />
      </div>
    )
  }
  if (!profile) return <p className="text-gray-400 text-sm">Nothing here yet</p>

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <h1 className="font-display text-xl font-bold text-gray-900 mb-1">{profile.name}</h1>
      <p className="text-gray-500 text-sm mb-6">{profile.role?.replace('_', ' ')}</p>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-6">
        <div className="card">
          <div className="text-2xl font-bold text-emp-primary">{stats?.weekHours ?? '—'}</div>
          <div className="text-xs text-gray-500 mt-1">Hours This Week</div>
        </div>
        <div className="card">
          <div className="text-2xl font-bold text-emp-primary">{profile.remainingLeaveDays ?? '—'}</div>
          <div className="text-xs text-gray-500 mt-1">Leave Days Remaining</div>
        </div>
        <div className="card">
          <div className="text-2xl font-bold text-emp-primary">{stats?.pendingTasks ?? '—'}</div>
          <div className="text-xs text-gray-500 mt-1">Pending Tasks</div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className={`card p-6${user?.authType === 'GOOGLE_ONLY' ? ' lg:col-span-3' : ' lg:col-span-2'}`}>
          <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Account Info</h2>
          <div className="grid grid-cols-2 gap-4 mb-6">
            <div>
              <span className="text-xs text-gray-500 block">Email</span>
              <span className="text-sm text-gray-900">{profile.email}</span>
            </div>
            <div>
              <span className="text-xs text-gray-500 block">Branch</span>
              <span className="text-sm text-gray-900">{profile.branchName || '—'}</span>
            </div>
            <div>
              <span className="text-xs text-gray-500 block">Wage Type</span>
              <span className="text-sm text-gray-900">{profile.wageType === 'DAILY' ? 'Daily Rate' : profile.wageType === 'HOURLY' ? 'Hourly Wage' : '—'}</span>
            </div>
            <div>
              <span className="text-xs text-gray-500 block">{profile.wageType === 'HOURLY' ? 'Hourly Wage' : 'Daily Rate'}</span>
              <span className="text-sm text-gray-900">
                {profile.wageType === 'DAILY' && profile.dailyRate ? `Rs. ${Number(profile.dailyRate).toLocaleString()}` : ''}
                {profile.wageType === 'HOURLY' && profile.hourlyWage ? `Rs. ${Number(profile.hourlyWage).toLocaleString()}` : ''}
                {!profile.wageType ? '—' : ''}
              </span>
            </div>
            <div>
              <span className="text-xs text-gray-500 block">Two-Factor Auth</span>
              <span className="text-sm text-gray-900">{profile.mfaEnabled ? 'Enabled' : 'Disabled'}</span>
            </div>
          </div>
          <button onClick={handleToggleMfa} disabled={mfaToggling} className={`text-sm font-medium px-3 py-1.5 rounded-lg border mb-6 ${profile.mfaEnabled ? 'text-red-600 border-red-200 hover:bg-red-50' : 'text-emerald-600 border-emerald-200 hover:bg-emerald-50'} disabled:opacity-50`}>
            {mfaToggling ? 'Updating...' : profile.mfaEnabled ? 'Disable MFA' : 'Enable MFA'}
          </button>
          <h3 className="font-display font-bold text-gray-900 mb-3">Edit Name</h3>
          <form onSubmit={handleSave} className="space-y-4">
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Name</label>
              <input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} className="input-field w-full" required />
            </div>
            <button type="submit" disabled={saving} className="btn-admin !bg-emp-primary hover:!bg-emerald-700">{saving ? 'Saving...' : 'Save Changes'}</button>
          </form>
        </div>
        {user?.authType !== 'GOOGLE_ONLY' && (
          <div className="card p-6">
            <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Change Password</h2>
            <form onSubmit={handleChangePassword} className="space-y-4">
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Current Password</label>
                <div className="relative">
                  <input type={showCurrentPass ? 'text' : 'password'} value={passForm.currentPassword} onChange={e => setPassForm(f => ({ ...f, currentPassword: e.target.value }))} className="input-field w-full pr-10" required />
                  <button type="button" onClick={() => setShowCurrentPass(!showCurrentPass)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                    {showCurrentPass ? (
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" /></svg>
                    ) : (
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                    )}
                  </button>
                </div>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">New Password</label>
                <div className="relative">
                  <input type={showNewPass ? 'text' : 'password'} value={passForm.newPassword} onChange={e => setPassForm(f => ({ ...f, newPassword: e.target.value }))} className="input-field w-full pr-10" required />
                  <button type="button" onClick={() => setShowNewPass(!showNewPass)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                    {showNewPass ? (
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" /></svg>
                    ) : (
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                    )}
                  </button>
                </div>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Confirm New Password</label>
                <div className="relative">
                  <input type={showConfirmPass ? 'text' : 'password'} value={passForm.confirmPassword} onChange={e => setPassForm(f => ({ ...f, confirmPassword: e.target.value }))} className="input-field w-full pr-10" required />
                  <button type="button" onClick={() => setShowConfirmPass(!showConfirmPass)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                    {showConfirmPass ? (
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" /></svg>
                    ) : (
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                    )}
                  </button>
                </div>
              </div>
              <button type="submit" disabled={changingPass} className="btn-admin !bg-emp-primary hover:!bg-emerald-700">{changingPass ? 'Changing...' : 'Change Password'}</button>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}
