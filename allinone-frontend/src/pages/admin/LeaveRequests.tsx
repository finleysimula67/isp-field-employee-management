import { useEffect, useState, useCallback } from 'react'
import { getLeaveRequests, reviewLeaveRequest, batchReviewLeaveRequests } from '../../api/leaveRequests'
import { getEmployees } from '../../api/employees'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const statusColors: Record<string, string> = {
  PENDING: 'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
}

export default function LeaveRequestsPage() {
  const [requests, setRequests] = useState<any[]>([])
  const [employees, setEmployees] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [filterStatus, setFilterStatus] = useState('')
  const [reviewingId, setReviewingId] = useState<number | null>(null)
  const [reviewForm, setReviewForm] = useState({ status: 'APPROVED', reviewComment: '' })
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [batchAction, setBatchAction] = useState('APPROVED')
  const [batchProcessing, setBatchProcessing] = useState(false)

  const fetchData = useCallback(() => {
    setLoading(true)
    const params: any = {}
    if (filterStatus) params.status = filterStatus
    Promise.all([getLeaveRequests(params), getEmployees()])
      .then(([reqRes, empRes]) => {
        setRequests(reqRes.data)
        setEmployees(empRes.data)
      })
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }, [filterStatus])

  useEffect(() => { fetchData() }, [fetchData])

  const empMap = new Map(employees.map((e: any) => [e.id, e]))

  const handleReview = async (id: number) => {
    try {
      await reviewLeaveRequest(id, reviewForm)
      setReviewingId(null)
      setReviewForm({ status: 'APPROVED', reviewComment: '' })
      setToast({ message: 'Review submitted', type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Failed to submit review', type: 'error' })
    }
  }

  const toggleSelect = (id: number) => {
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id])
  }

  const toggleSelectAll = () => {
    if (selectedIds.length === requests.length) {
      setSelectedIds([])
    } else {
      setSelectedIds(requests.map(r => r.id))
    }
  }

  const handleBatchReview = async () => {
    if (selectedIds.length === 0) return
    setBatchProcessing(true)
    try {
      await batchReviewLeaveRequests({ ids: selectedIds, status: batchAction })
      setToast({ message: `${selectedIds.length} request(s) ${batchAction.toLowerCase()}`, type: 'success' })
      setSelectedIds([])
      fetchData()
    } catch {
      setToast({ message: 'Batch review failed', type: 'error' })
    } finally {
      setBatchProcessing(false)
    }
  }

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Leave Requests</h1>
      </div>
      <div className="card p-4 mb-6">
        <div className="flex gap-4 items-end">
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Status</label>
            <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)} className="input-field">
              <option value="">All</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>
          <button onClick={fetchData} className="btn-admin">Filter</button>
        </div>
      </div>
      {selectedIds.length > 0 && (
        <div className="card p-3 mb-4 flex items-center gap-4 bg-blue-50 border-blue-200">
          <span className="text-sm font-medium text-blue-800">{selectedIds.length} selected</span>
          <select value={batchAction} onChange={e => setBatchAction(e.target.value)} className="input-field text-xs w-auto">
            <option value="APPROVED">Approve</option>
            <option value="REJECTED">Reject</option>
          </select>
          <button onClick={handleBatchReview} disabled={batchProcessing} className="btn-admin text-xs">
            {batchProcessing ? 'Processing...' : `Apply ${batchAction}`}
          </button>
          <button onClick={() => setSelectedIds([])} className="btn-ghost text-xs">Clear</button>
        </div>
      )}
      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100">
              <th className="text-left py-3 px-4 w-10">
                <input type="checkbox" onChange={toggleSelectAll} checked={selectedIds.length === requests.length && requests.length > 0} className="rounded" />
              </th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Employee</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Type</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Dates</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Duration</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Remaining</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <Skeleton variant="table-row" count={5} />
            ) : requests.length === 0 ? (
              <tr><td colSpan={8} className="py-8 text-center text-gray-400">No requests found</td></tr>
            ) : requests.map((req: any) => {
              const emp = empMap.get(req.employeeId)
              return (
                <tr key={req.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="py-3 px-4">
                    <input type="checkbox" checked={selectedIds.includes(req.id)} onChange={() => toggleSelect(req.id)} className="rounded" />
                  </td>
                  <td className="py-3 px-4 font-medium">{emp?.name || `#${req.employeeId}`}</td>
                  <td className="py-3 px-4 text-gray-500">{req.leaveType}</td>
                  <td className="py-3 px-4 text-gray-500">{req.startDate} → {req.endDate}</td>
                  <td className="py-3 px-4 text-gray-500">{req.durationDays ?? '—'} day(s)</td>
                  <td className="py-3 px-4 text-gray-500">{emp?.remainingLeaveDays ?? '—'}</td>
                  <td className="py-3 px-4">
                    <span className={statusColors[req.status] || 'badge-pending'}>{req.status}</span>
                  </td>
                  <td className="py-3 px-4">
                    {reviewingId === req.id ? (
                      <div className="flex flex-col gap-2">
                        <select value={reviewForm.status} onChange={e => setReviewForm(f => ({ ...f, status: e.target.value }))} className="input-field text-xs">
                          <option value="APPROVED">Approve</option>
                          <option value="REJECTED">Reject</option>
                        </select>
                        <textarea value={reviewForm.reviewComment} onChange={e => setReviewForm(f => ({ ...f, reviewComment: e.target.value }))} placeholder="Comment" className="input-field text-xs" rows={2} />
                        <div className="flex gap-2">
                          <button onClick={() => handleReview(req.id)} className="btn-admin text-xs">Submit</button>
                          <button onClick={() => { setReviewingId(null); setReviewForm({ status: 'APPROVED', reviewComment: '' }) }} className="btn-ghost text-xs">Cancel</button>
                        </div>
                      </div>
                    ) : (
                      <button onClick={() => setReviewingId(req.id)} className="btn-ghost text-xs">Review</button>
                    )}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}
