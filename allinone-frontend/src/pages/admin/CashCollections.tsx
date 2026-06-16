import { useEffect, useState, useCallback } from 'react'
import { getCashCollections, reviewCashCollection, batchReviewCashCollections } from '../../api/cashCollections'
import { getEmployees } from '../../api/employees'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const statusColors: Record<string, string> = {
  PENDING: 'badge-pending',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected',
  NEEDS_REVISION: 'badge-revision',
}

export default function CashCollectionsPage() {
  const [collections, setCollections] = useState<any[]>([])
  const [employees, setEmployees] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [filterStatus, setFilterStatus] = useState('')
  const [filterEmployee, setFilterEmployee] = useState('')
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
    if (filterEmployee) params.employeeId = Number(filterEmployee)
    Promise.all([getCashCollections(params), getEmployees()])
      .then(([colRes, empRes]) => {
        setCollections(colRes.data)
        setEmployees(empRes.data)
      })
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }, [filterStatus, filterEmployee])

  useEffect(() => { fetchData() }, [fetchData])

  const empMap = new Map(employees.map((e: any) => [e.id, e]))

  const handleReview = async (id: number) => {
    try {
      await reviewCashCollection(id, {
        status: reviewForm.status,
        reviewComment: reviewForm.reviewComment,
      })
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
    if (selectedIds.length === collections.length) {
      setSelectedIds([])
    } else {
      setSelectedIds(collections.map(c => c.id))
    }
  }

  const handleBatchReview = async () => {
    if (selectedIds.length === 0) return
    setBatchProcessing(true)
    try {
      await batchReviewCashCollections({ ids: selectedIds, status: batchAction })
      setToast({ message: `${selectedIds.length} collection(s) ${batchAction.toLowerCase()}`, type: 'success' })
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
        <h1 className="font-display text-2xl font-bold text-gray-900">Cash Collections</h1>
      </div>
      <div className="card p-4 mb-6">
        <div className="flex flex-wrap gap-3 items-end">
          <div className="flex-1 min-w-[120px]">
            <label className="text-xs font-medium text-gray-500 block mb-1">Status</label>
            <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)} className="input-field w-full">
              <option value="">All</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
              <option value="NEEDS_REVISION">Needs Revision</option>
            </select>
          </div>
          <div className="flex-1 min-w-[140px]">
            <label className="text-xs font-medium text-gray-500 block mb-1">Employee</label>
            <select value={filterEmployee} onChange={e => setFilterEmployee(e.target.value)} className="input-field w-full">
              <option value="">All</option>
              {employees.map((e: any) => (
                <option key={e.id} value={e.id}>{e.name}</option>
              ))}
            </select>
          </div>
          <button onClick={fetchData} className="btn-admin">Filter</button>
        </div>
      </div>
      {selectedIds.length > 0 && (
        <div className="card p-3 mb-4 flex flex-wrap items-center gap-3 bg-blue-50 border-blue-200">
          <span className="text-sm font-medium text-blue-800">{selectedIds.length} selected</span>
          <select value={batchAction} onChange={e => setBatchAction(e.target.value)} className="input-field text-xs w-auto">
            <option value="APPROVED">Approve</option>
            <option value="REJECTED">Reject</option>
            <option value="NEEDS_REVISION">Needs Revision</option>
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
                <input type="checkbox" onChange={toggleSelectAll} checked={selectedIds.length === collections.length && collections.length > 0} className="rounded" />
              </th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Employee</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Customer</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Amount</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Payment</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Service</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Location</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <Skeleton variant="table-row" count={5} />
            ) : collections.length === 0 ? (
              <tr><td colSpan={9} className="py-8 text-center text-gray-400">Nothing here yet</td></tr>
            ) : collections.map((c: any) => (
              <tr key={c.id} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="py-3 px-4">
                  <input type="checkbox" checked={selectedIds.includes(c.id)} onChange={() => toggleSelect(c.id)} className="rounded" />
                </td>
                <td className="py-3 px-4 font-medium">{empMap.get(c.employeeId)?.name || `#${c.employeeId}`}</td>
                <td className="py-3 px-4">
                  <div className="flex flex-col">
                    <span className="font-medium text-gray-900">{c.customerName}</span>
                    {c.customerPhone && <span className="text-xs text-gray-400">{c.customerPhone}</span>}
                  </div>
                </td>
                <td className="py-3 px-4 font-medium">Rs. {Number(c.amount).toLocaleString()}</td>
                <td className="py-3 px-4 text-gray-500">{c.paymentMethod?.replace(/_/g, ' ')}</td>
                <td className="py-3 px-4 text-gray-500">{c.serviceType?.replace(/_/g, ' ')}</td>
                <td className="py-3 px-4">
                  {c.locationLat != null && c.locationLng != null ? (
                    <a href={`https://www.google.com/maps?q=${c.locationLat},${c.locationLng}`} target="_blank" rel="noopener noreferrer" className="text-xs text-brand-600 hover:text-brand-700 underline">View on Map</a>
                  ) : (
                    <span className="text-xs text-gray-400">—</span>
                  )}
                </td>
                <td className="py-3 px-4">
                  <span className={statusColors[c.status] || 'badge-pending'}>{c.status?.replace(/_/g, ' ')}</span>
                </td>
                <td className="py-3 px-4">
                  {reviewingId === c.id ? (
                    <div className="flex flex-col gap-2">
                      <select value={reviewForm.status} onChange={e => setReviewForm(f => ({ ...f, status: e.target.value }))} className="input-field text-xs">
                        <option value="APPROVED">Approve</option>
                        <option value="REJECTED">Reject</option>
                        <option value="NEEDS_REVISION">Needs Revision</option>
                      </select>
                      <textarea value={reviewForm.reviewComment} onChange={e => setReviewForm(f => ({ ...f, reviewComment: e.target.value }))} placeholder="Comment" className="input-field text-xs" rows={2} />
                      <div className="flex gap-2">
                        <button onClick={() => handleReview(c.id)} className="btn-admin text-xs">Submit</button>
                        <button onClick={() => { setReviewingId(null); setReviewForm({ status: 'APPROVED', reviewComment: '' }) }} className="btn-ghost text-xs">Cancel</button>
                      </div>
                    </div>
                  ) : (
                    <button onClick={() => setReviewingId(c.id)} className="btn-ghost text-xs">Review</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
