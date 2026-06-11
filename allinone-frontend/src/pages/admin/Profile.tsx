import { useEffect, useState } from 'react'
import { getProfile, updateProfile, changePassword } from '../../api/profile'
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
          </div>
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
                <input type="password" value={passForm.currentPassword} onChange={e => setPassForm(f => ({ ...f, currentPassword: e.target.value }))} className="input-field w-full" required />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">New Password</label>
                <input type="password" value={passForm.newPassword} onChange={e => setPassForm(f => ({ ...f, newPassword: e.target.value }))} className="input-field w-full" required />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Confirm New Password</label>
                <input type="password" value={passForm.confirmPassword} onChange={e => setPassForm(f => ({ ...f, confirmPassword: e.target.value }))} className="input-field w-full" required />
              </div>
              <button type="submit" disabled={changingPass} className="btn-admin">{changingPass ? 'Changing...' : 'Change Password'}</button>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}
