import { useState } from 'react'
import { purgeAllData } from '../../api/admin'
import DeleteConfirmModal from '../../components/DeleteConfirmModal'

export default function PurgePage() {
  const [loading, setLoading] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [result, setResult] = useState<{ deletedCounts: Record<string, number>; totalDeleted: number } | null>(null)
  const [error, setError] = useState('')

  const handlePurge = async () => {
    setLoading(true)
    setError('')
    setShowConfirm(false)
    try {
      const res = await purgeAllData()
      if (res.success) setResult(res.data)
      else setError(res.message || 'Purge failed')
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Purge failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-xl font-bold text-gray-900">Factory Reset</h1>
      </div>

      <div className="bg-white rounded-xl border border-red-200 shadow-sm p-6">
        <div className="flex items-start gap-3 mb-4">
          <div className="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
            <span className="text-lg">⚠️</span>
          </div>
          <div>
            <h2 className="font-display text-lg font-bold text-gray-900 mb-1">Purge All Transactional Data</h2>
            <p className="text-sm text-gray-500 leading-relaxed">
              This will <strong>permanently delete</strong> all transactional records from the system including:
              daily logs, cash collections, leave requests, tasks, salary advances, payroll records, holidays,
              notifications, audit logs, recycle bin items, and monthly lockouts.
            </p>
            <p className="text-sm text-gray-500 leading-relaxed mt-2">
              <strong>Preserved:</strong> Employee records, branches, and email allow list.
            </p>
            <p className="text-sm text-red-600 font-medium mt-2">
              This action CANNOT be undone. Make sure you have backed up any important data first.
            </p>
          </div>
        </div>

        <button
          onClick={() => setShowConfirm(true)}
          disabled={loading}
          className="bg-red-600 hover:bg-red-700 text-white text-sm font-medium px-6 py-2.5 rounded-lg disabled:opacity-50 transition-colors"
        >
          {loading ? 'Purging...' : 'Purge All Data'}
        </button>

        {error && (
          <div className="mt-4 bg-red-50 text-red-700 text-sm px-4 py-2 rounded-md">{error}</div>
        )}

        {result && (
          <div className="mt-6 bg-green-50 border border-green-200 rounded-lg p-4">
            <h3 className="font-medium text-green-800 mb-2">Purge Complete — {result.totalDeleted} records deleted</h3>
            <table className="text-sm text-green-700 w-full max-w-md">
              <tbody>
                {Object.entries(result.deletedCounts).map(([table, count]) => (
                  <tr key={table} className="border-b border-green-100 last:border-0">
                    <td className="py-1 capitalize">{table.replace(/([A-Z])/g, ' $1').trim()}</td>
                    <td className="py-1 text-right font-mono">{count}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <DeleteConfirmModal
        open={showConfirm}
        title="Purge All Transactional Data?"
        message="This will permanently delete ALL transactional records. Employee records, branches, and email allow list will be preserved. This action CANNOT be undone."
        count={-1}
        onConfirm={handlePurge}
        onCancel={() => setShowConfirm(false)}
      />
    </div>
  )
}
