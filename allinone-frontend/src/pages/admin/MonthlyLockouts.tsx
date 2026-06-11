import { useEffect, useState } from 'react'
import { getAllLockouts, lockMonth, unlockMonth } from '../../api/monthlyLockouts'
import { useAuth } from '../../contexts/AuthContext'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

export default function MonthlyLockoutsPage() {
  const { user } = useAuth()
  const isSuperAdmin = user?.role === 'SUPER_ADMIN'
  const [lockouts, setLockouts] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [lockYearMonth, setLockYearMonth] = useState('')
  const [unlocking, setUnlocking] = useState<{ id: number; yearMonth: string } | null>(null)
  const [unlockReason, setUnlockReason] = useState('')
  const [lockSubmitting, setLockSubmitting] = useState(false)

  const fetchData = () => {
    setLoading(true)
    getAllLockouts()
      .then(res => { setLockouts(res.data || []); setLoading(false) })
      .catch(() => { setToast({ message: 'Failed to load', type: 'error' }); setLoading(false) })
  }

  useEffect(() => { fetchData() }, [])

  const handleLock = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!lockYearMonth) return
    setLockSubmitting(true)
    setToast(null)
    try {
      await lockMonth(lockYearMonth)
      setLockYearMonth('')
      setToast({ message: 'Month locked', type: 'success' })
      fetchData()
    } catch { setToast({ message: 'Failed to lock month', type: 'error' }) }
    finally { setLockSubmitting(false) }
  }

  const handleUnlock = async () => {
    if (!unlocking || !unlockReason) return
    try {
      await unlockMonth(unlocking.yearMonth, unlockReason)
      setToast({ message: 'Month unlocked', type: 'success' })
      setUnlocking(null)
      setUnlockReason('')
      fetchData()
    } catch { setToast({ message: 'Failed to unlock month', type: 'error' }) }
  }

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Monthly Lockouts</h1>
      </div>
      {isSuperAdmin && (
        <div className="card p-4 mb-6">
          <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Manual Lock Month</h2>
          <form onSubmit={handleLock} className="flex gap-4 items-end">
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Year-Month</label>
              <input type="month" value={lockYearMonth} onChange={e => setLockYearMonth(e.target.value)} className="input-field" required />
            </div>
            <button type="submit" disabled={lockSubmitting} className="btn-admin">{lockSubmitting ? 'Locking...' : 'Lock Month'}</button>
          </form>
        </div>
      )}
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100">
                <th className="text-left py-3 px-4 font-medium text-gray-500">Year-Month</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Locked At</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Locked By</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Lock Day</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Unlocked</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Unlock Reason</th>
                {isSuperAdmin && <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>}
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <Skeleton variant="table-row" count={5} />
              ) : lockouts.length === 0 ? (
                <tr><td colSpan={isSuperAdmin ? 8 : 7} className="py-8 text-center text-gray-400">No lockout records</td></tr>
              ) : lockouts.map((l: any) => (
                <tr key={l.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="py-3 px-4 font-medium">{l.yearMonth}</td>
                  <td className="py-3 px-4">
                    <span className={`badge ${l.isLocked && !l.isUnlocked ? 'badge-warning' : 'badge-success'}`}>
                      {l.isLocked && !l.isUnlocked ? 'Locked' : 'Open'}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-xs">{l.lockedAt ? new Date(l.lockedAt).toLocaleString() : '-'}</td>
                  <td className="py-3 px-4">{l.lockedByName || '-'}</td>
                  <td className="py-3 px-4">{l.lockDay}</td>
                  <td className="py-3 px-4">{l.isUnlocked ? new Date(l.unlockedAt).toLocaleString() : '-'}</td>
                  <td className="py-3 px-4 text-xs max-w-[200px] truncate">{l.unlockedReason || '-'}</td>
                  {isSuperAdmin && (
                    <td className="py-3 px-4">
                      {l.isLocked && !l.isUnlocked && (
                        <button onClick={() => setUnlocking({ id: l.id, yearMonth: l.yearMonth })} className="text-xs text-amber-600 hover:text-amber-800">Unlock</button>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {unlocking && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={e => { if (e.target === e.currentTarget) { setUnlocking(null); setUnlockReason('') } }}>
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6" onClick={e => e.stopPropagation()}>
            <h2 className="font-display text-lg font-bold text-gray-900 mb-2">Unlock {unlocking.yearMonth}</h2>
            <p className="text-sm text-gray-500 mb-4">Provide a reason for unlocking this month.</p>
            <textarea value={unlockReason} onChange={e => setUnlockReason(e.target.value)} className="input-field w-full mb-4" rows={3} placeholder="Reason for unlock..." required />
            <div className="flex justify-end gap-3">
              <button onClick={() => { setUnlocking(null); setUnlockReason('') }} className="btn-secondary">Cancel</button>
              <button onClick={handleUnlock} disabled={!unlockReason} className="btn-admin">Confirm Unlock</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
