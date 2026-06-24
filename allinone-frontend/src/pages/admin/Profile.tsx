import { useEffect, useState } from 'react'
import { getProfile, updateProfile, changePassword } from '../../api/profile'
import { enableMfa, disableMfa } from '../../api/auth'
import { useAuth } from '../../contexts/AuthContext'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

export default function ProfilePage() {
  const { user } = useAuth()
  const [profile, setProfile] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState({ name: '', wageType: '', dailyRate: '', hourlyWage: '' })
  const [passForm, setPassForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [saving, setSaving] = useState(false)
  const [changingPass, setChangingPass] = useState(false)
  const [showCurrentPass, setShowCurrentPass] = useState(false)
  const [showNewPass, setShowNewPass] = useState(false)
  const [showConfirmPass, setShowConfirmPass] = useState(false)
  const [mfaToggling, setMfaToggling] = useState(false)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  useEffect(() => {
    getProfile().then((res) => {
      const p = res.data || res
      setProfile(p)
      setForm({ name: p.name || '', wageType: p.wageType || '', dailyRate: p.dailyRate?.toString() || '', hourlyWage: p.hourlyWage?.toString() || '' })
      setLoading(false)
    }).catch(() => setLoading(false))
  }, [])

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setToast(null)
    try {
      await updateProfile({
        name: form.name,
        wageType: form.wageType || null,
        dailyRate: form.dailyRate ? Number(form.dailyRate) : null,
        hourlyWage: form.hourlyWage ? Number(form.hourlyWage) : null,
      })
      setToast({ message: 'Profile updated successfully', type: 'success' })
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
      setToast({ message: 'Password changed successfully', type: 'success' })
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

  if (loading) return <Skeleton variant="card" count={3} />
  if (!profile) return <p className="text-gray-400 text-sm">Nothing here yet</p>

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <h1 className="font-display text-2xl font-bold text-gray-900 mb-6">Profile</h1>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card p-6">
          <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Account Info</h2>
          <div className="space-y-3 mb-6">
            <div><span className="text-xs text-gray-500 block">Name</span><span className="text-sm text-gray-900">{profile.name}</span></div>
            <div><span className="text-xs text-gray-500 block">Email</span><span className="text-sm text-gray-900">{profile.email}</span></div>
            <div><span className="text-xs text-gray-500 block">Role</span><span className="text-sm text-gray-900">{profile.role?.replace('_', ' ')}</span></div>
            <div><span className="text-xs text-gray-500 block">Branch</span><span className="text-sm text-gray-900">{profile.branchName || '—'}</span></div>
            <div>
              <span className="text-xs text-gray-500 block">Two-Factor Auth</span>
              <span className="text-sm text-gray-900">{profile.mfaEnabled ? 'Enabled' : 'Disabled'}</span>
            </div>
          </div>
          <button onClick={handleToggleMfa} disabled={mfaToggling} className={`text-sm font-medium px-3 py-1.5 rounded-lg border mb-6 ${profile.mfaEnabled ? 'text-red-600 border-red-200 hover:bg-red-50' : 'text-brand-600 border-brand-200 hover:bg-brand-50'} disabled:opacity-50`}>
            {mfaToggling ? 'Updating...' : profile.mfaEnabled ? 'Disable MFA' : 'Enable MFA'}
          </button>
          <h3 className="font-display font-bold text-gray-900 mb-3">Edit Profile</h3>
          <form onSubmit={handleSave} className="space-y-4">
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Name</label>
              <input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} className="input-field w-full" required />
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Wage Type</label>
              <select value={form.wageType} onChange={e => setForm(f => ({ ...f, wageType: e.target.value }))} className="input-field w-full">
                <option value="">None</option>
                <option value="DAILY">Daily</option>
                <option value="HOURLY">Hourly</option>
              </select>
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Daily Rate</label>
              <input type="number" value={form.dailyRate} onChange={e => setForm(f => ({ ...f, dailyRate: e.target.value }))} className="input-field w-full" />
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Hourly Wage</label>
              <input type="number" value={form.hourlyWage} onChange={e => setForm(f => ({ ...f, hourlyWage: e.target.value }))} className="input-field w-full" />
            </div>
            <button type="submit" disabled={saving} className="btn-admin">{saving ? 'Saving...' : 'Save Changes'}</button>
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
              <button type="submit" disabled={changingPass} className="btn-admin">{changingPass ? 'Changing...' : 'Change Password'}</button>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}
