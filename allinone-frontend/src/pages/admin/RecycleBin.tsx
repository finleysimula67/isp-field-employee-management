import { useEffect, useState } from 'react'
import { getRecycleBinItems, restoreRecycleBinItem, permanentDeleteRecycleBinItem, getRecycleBinCount } from '../../api/recycleBin'
import Toast from '../../components/Toast'
import DeleteConfirmModal from '../../components/DeleteConfirmModal'
import Skeleton from '../../components/Skeleton'

const entityTypes = ['', 'DailyLog', 'CashCollection', 'LeaveRequest', 'Task', 'PayrollRecord', 'SalaryAdvance', 'Branch', 'Holiday', 'Notification', 'MonthlyLockout', 'EmailAllowList', 'AuditLog']

export default function RecycleBinPage() {
  const [items, setItems] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [entityType, setEntityType] = useState('')
  const [page, setPage] = useState(0)
  const [totalItems, setTotalItems] = useState(0)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [confirmModal, setConfirmModal] = useState<{ id: number; type: 'restore' | 'permanent' } | null>(null)
  const [actionLoading, setActionLoading] = useState(false)

  const fetchData = () => {
    setLoading(true)
    Promise.all([
      getRecycleBinItems(entityType || undefined, page, 50),
      getRecycleBinCount(entityType || undefined)
    ]).then(([itemsData, countData]) => {
      setItems(itemsData.content || itemsData)
      setTotalItems(countData.count || 0)
      setLoading(false)
    }).catch(() => {
      setToast({ message: 'Failed to load recycle bin', type: 'error' })
      setLoading(false)
    })
  }

  useEffect(() => { fetchData() }, [page, entityType])

  const handleRestore = async () => {
    if (!confirmModal || confirmModal.type !== 'restore') return
    setActionLoading(true)
    try {
      await restoreRecycleBinItem(confirmModal.id)
      setToast({ message: 'Record restored successfully', type: 'success' })
      setConfirmModal(null)
      fetchData()
    } catch { setToast({ message: 'Failed to restore record', type: 'error' }) }
    finally { setActionLoading(false) }
  }

  const handlePermanentDelete = async () => {
    if (!confirmModal || confirmModal.type !== 'permanent') return
    setActionLoading(true)
    try {
      await permanentDeleteRecycleBinItem(confirmModal.id)
      setToast({ message: 'Record permanently deleted', type: 'success' })
      setConfirmModal(null)
      fetchData()
    } catch { setToast({ message: 'Failed to permanently delete record', type: 'error' }) }
    finally { setActionLoading(false) }
  }

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <DeleteConfirmModal
        open={!!confirmModal}
        title={confirmModal?.type === 'restore' ? 'Restore Record' : 'Permanently Delete Record'}
        message={confirmModal?.type === 'restore'
          ? 'This will restore the record to its original state.'
          : 'This action CANNOT be undone. The record will be permanently removed.'}
        onConfirm={confirmModal?.type === 'restore' ? handleRestore : handlePermanentDelete}
        onCancel={() => setConfirmModal(null)}
        loading={actionLoading}
        confirmLabel={confirmModal?.type === 'restore' ? 'Restore' : undefined}
      />
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Recycle Bin</h1>
        <span className="text-sm text-gray-500">{totalItems} deleted records</span>
      </div>
      <div className="card p-4 mb-6">
        <div className="flex flex-wrap gap-4 items-end">
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Entity Type</label>
            <select value={entityType} onChange={e => { setEntityType(e.target.value); setPage(0) }} className="input-field">
              <option value="">All Types</option>
              {entityTypes.filter(Boolean).map(t => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>
          <button onClick={fetchData} className="btn-admin">Refresh</button>
        </div>
      </div>
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100">
                <th className="text-left py-3 px-4 font-medium text-gray-500">Type</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Entity ID</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Deleted At</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Deleted By</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <Skeleton variant="table-row" count={5} />
              ) : items.length === 0 ? (
                <tr><td colSpan={5} className="py-8 text-center text-gray-400">Recycle bin is empty</td></tr>
              ) : items.map((item: any) => (
                <tr key={item.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="py-3 px-4"><span className="badge badge-info">{item.entityType}</span></td>
                  <td className="py-3 px-4">{item.entityId}</td>
                  <td className="py-3 px-4 text-xs">{item.deletedAt ? new Date(item.deletedAt).toLocaleString() : '-'}</td>
                  <td className="py-3 px-4">{item.deletedBy?.name || 'System'}</td>
                  <td className="py-3 px-4">
                    <div className="flex gap-2">
                      <button onClick={() => setConfirmModal({ id: item.id, type: 'restore' })}
                        className="text-xs font-medium text-green-600 hover:text-green-800 px-2 py-1 rounded hover:bg-green-50">
                        Restore
                      </button>
                      <button onClick={() => setConfirmModal({ id: item.id, type: 'permanent' })}
                        className="text-xs font-medium text-red-600 hover:text-red-800 px-2 py-1 rounded hover:bg-red-50">
                        Delete Permanently
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {totalItems > 50 && (
        <div className="flex justify-center gap-4 mt-6">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
            className="btn-admin">Previous</button>
          <span className="text-sm text-gray-500 self-center">Page {page + 1}</span>
          <button disabled={items.length < 50} onClick={() => setPage(p => p + 1)}
            className="btn-admin">Next</button>
        </div>
      )}
    </div>
  )
}
