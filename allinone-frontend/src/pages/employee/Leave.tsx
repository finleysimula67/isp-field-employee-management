import { useEffect, useState } from 'react'
import { getMyLeaveRequests, createLeaveRequest, deleteLeaveRequest, batchDeleteLeaveRequests } from '../../api/leaveRequests'
import { useAuth } from '../../contexts/AuthContext'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'
import DeleteConfirmModal from '../../components/DeleteConfirmModal'

const statusColors: Record<string, string> = {
  PENDING: 'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
}

const initialForm = {
  leaveType: 'ANNUAL',
  startDate: '',
  endDate: '',
  reason: '',
}

export default function LeavePage() {
  const { user } = useAuth()
  const [requests, setRequests] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState(initialForm)
  const [submitting, setSubmitting] = useState(false)
  const [filterStatus, setFilterStatus] = useState('')
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null)
  const [confirmBatchDelete, setConfirmBatchDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)

  const fetchRequests = () => {
    setLoading(true)
    const params: any = {}
    if (filterStatus) params.status = filterStatus
    getMyLeaveRequests(params)
      .then(res => setRequests(res.data))
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchRequests() }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.startDate || !form.endDate) { setToast({ message: 'Start and end dates are required', type: 'error' }); return }
    setSubmitting(true)
    setToast(null)
    try {
      await createLeaveRequest({ ...form, employeeId: user?.id })
      setForm(initialForm)
      fetchRequests()
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to submit leave request'
      setToast({ message: msg, type: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  const toggleSelect = (id: number) => {
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id])
  }

  const handleDelete = async () => {
    if (confirmDeleteId === null) return
    setDeleting(true)
    try {
      await deleteLeaveRequest(confirmDeleteId)
      setConfirmDeleteId(null)
      setToast({ message: 'Leave request deleted', type: 'success' })
      fetchRequests()
    } catch {
      setToast({ message: 'Failed to delete', type: 'error' })
    } finally {
      setDeleting(false)
    }
  }

  const handleBatchDelete = async () => {
    if (selectedIds.length === 0) return
    setDeleting(true)
    try {
      await batchDeleteLeaveRequests({ ids: selectedIds })
      setConfirmBatchDelete(false)
      setSelectedIds([])
      setToast({ message: `${selectedIds.length} request(s) deleted`, type: 'success' })
      fetchRequests()
    } catch {
      setToast({ message: 'Batch delete failed', type: 'error' })
    } finally {
      setDeleting(false)
    }
  }

  const canSelect = (req: any) => req.status === 'PENDING'

  return (
    <div>
      <h1 className="font-display text-xl font-bold text-gray-900 mb-6">Leave Requests</h1>
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        <div className="lg:col-span-2 card p-4">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-display text-base font-bold text-gray-900">Request Leave</h2>
            <span className="text-xs text-gray-500">Remaining: <strong>{user?.remainingLeaveDays ?? 0} days</strong></span>
          </div>
          <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-2">Leave Type</label>
              <div className="flex flex-wrap gap-4">
                {['ANNUAL', 'SICK', 'PERSONAL'].map(t => (
                  <label key={t} className="flex items-center gap-2 text-sm text-gray-700">
                    <input type="radio" name="leaveType" value={t} checked={form.leaveType === t} onChange={e => setForm(f => ({ ...f, leaveType: e.target.value }))} />
                    {t.charAt(0) + t.slice(1).toLowerCase()}
                  </label>
                ))}
              </div>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Start Date</label>
                <input type="date" value={form.startDate} onChange={e => setForm(f => ({ ...f, startDate: e.target.value }))} className="input-field w-full" required />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">End Date</label>
                <input type="date" value={form.endDate} onChange={e => setForm(f => ({ ...f, endDate: e.target.value }))} className="input-field w-full" required />
              </div>
            </div>
            <div>
              <label className="text-xs font-medium text-gray-500 block mb-1">Reason</label>
              <textarea value={form.reason} onChange={e => setForm(f => ({ ...f, reason: e.target.value }))} className="input-field w-full" rows={3} />
            </div>
            <button type="submit" disabled={submitting} className="btn-primary w-full">{submitting ? 'Submitting...' : 'Submit Request'}</button>
          </form>
        </div>
        <div className="lg:col-span-3 card p-4">
          <h2 className="font-display text-base font-bold text-gray-900 mb-4">My Leave Requests</h2>
          {selectedIds.length > 0 && (
            <div className="mb-3 p-2 bg-blue-50 border border-blue-200 rounded-lg flex items-center gap-3">
              <span className="text-sm font-medium text-blue-800">{selectedIds.length} selected</span>
              <button onClick={() => setSelectedIds([])} className="btn-ghost text-xs">Clear</button>
              <button onClick={() => setConfirmBatchDelete(true)} className="text-xs bg-red-50 text-red-600 px-3 py-1.5 rounded-lg hover:bg-red-100 font-medium ml-auto">Delete Selected</button>
            </div>
          )}
          <div className="mb-4">
            <select value={filterStatus} onChange={e => { setFilterStatus(e.target.value); fetchRequests() }} className="input-field w-full max-w-xs">
              <option value="">All Status</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>
          {loading ? (
            <Skeleton variant="table-row" count={5} />
          ) : requests.length === 0 ? (
            <p className="text-gray-400 text-sm text-center py-4">No requests yet</p>
          ) : (
            <div className="space-y-2">
              {requests.map((req: any) => (
                <div key={req.id} className="flex items-center gap-2 py-2 border-b border-gray-50">
                  <input
                    type="checkbox"
                    checked={selectedIds.includes(req.id)}
                    onChange={() => toggleSelect(req.id)}
                    className="rounded shrink-0"
                    disabled={!canSelect(req)}
                  />
                  <div className="flex items-center justify-between flex-1">
                    <div>
                      <p className="text-sm font-medium text-gray-900">{req.leaveType} • {req.durationDays ?? '—'} day(s)</p>
                      <p className="text-xs text-gray-500">{req.startDate} → {req.endDate}</p>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <span className={statusColors[req.status] || 'badge-pending'}>{req.status}</span>
                      {canSelect(req) && (
                        <button onClick={() => setConfirmDeleteId(req.id)} className="text-xs text-red-500 hover:text-red-700">✕</button>
                      )}
                    </div>
                  </div>
                  {req.reviewComment && (
                    <p className="text-xs text-gray-400 mt-1">Review: {req.reviewComment}</p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
      <DeleteConfirmModal open={confirmDeleteId !== null} title="Delete Leave Request?" message="This leave request will be soft-deleted and moved to the Recycle Bin. This action can be undone." onConfirm={handleDelete} onCancel={() => setConfirmDeleteId(null)} loading={deleting} />
      <DeleteConfirmModal open={confirmBatchDelete} title={`Delete ${selectedIds.length} Requests?`} message="These leave requests will be soft-deleted and moved to the Recycle Bin." count={selectedIds.length} onConfirm={handleBatchDelete} onCancel={() => setConfirmBatchDelete(false)} loading={deleting} />
    </div>
  )
}