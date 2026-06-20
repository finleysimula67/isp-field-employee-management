import { useState, useEffect } from 'react'
import { transferOwnershipFull } from '../../api/admin'
import { getEmployees } from '../../api/employees'
import DeleteConfirmModal from '../../components/DeleteConfirmModal'
import type { Employee } from '../../types'

export default function TransferOwnershipPage() {
  const [employees, setEmployees] = useState<Employee[]>([])
  const [sourceId, setSourceId] = useState<number | ''>('')
  const [targetId, setTargetId] = useState<number | ''>('')
  const [deleteSource, setDeleteSource] = useState(false)
  const [loading, setLoading] = useState(false)
  const [fetching, setFetching] = useState(true)
  const [showConfirm, setShowConfirm] = useState(false)
  const [result, setResult] = useState<any>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getEmployees()
      .then((res) => { if (res.success) setEmployees(res.data) })
      .catch(() => {})
      .finally(() => setFetching(false))
  }, [])

  const activeEmployees = employees.filter((e) => e.isActive)

  const sourceEmp = employees.find((e) => e.id === sourceId)
  const targetEmp = employees.find((e) => e.id === targetId)
  const transferCount = result?.totalTransferred ?? 0

  const handleTransfer = async () => {
    setLoading(true)
    setError('')
    setShowConfirm(false)
    try {
      const res = await transferOwnershipFull({
        sourceEmployeeId: sourceId as number,
        targetEmployeeId: targetId as number,
        deleteSource,
      })
      if (res.success) {
        setResult(res.data)
        setTimeout(() => window.location.reload(), 2000)
      } else {
        setError(res.message || 'Transfer failed')
      }
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Transfer failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-xl font-bold text-gray-900">Transfer Ownership</h1>
      </div>

      <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 max-w-2xl">
        <p className="text-sm text-gray-500 mb-6 leading-relaxed">
          Transfer all records (daily logs, cash collections, leave requests, tasks, salary advances,
          payroll records, notifications, audit logs, and more) from one employee to another.
        </p>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Source Employee</label>
            <select
              value={sourceId}
              onChange={(e) => setSourceId(e.target.value ? Number(e.target.value) : '')}
              className="input-field w-full"
              disabled={fetching}
            >
              <option value="">{fetching ? 'Loading...' : 'Select source employee'}</option>
              {activeEmployees.map((emp) => (
                <option key={emp.id} value={emp.id}>
                  {emp.name} ({emp.email}) — {emp.role}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Target Employee</label>
            <select
              value={targetId}
              onChange={(e) => setTargetId(e.target.value ? Number(e.target.value) : '')}
              className="input-field w-full"
              disabled={fetching}
            >
              <option value="">{fetching ? 'Loading...' : 'Select target employee'}</option>
              {activeEmployees
                .filter((emp) => emp.id !== sourceId)
                .map((emp) => (
                  <option key={emp.id} value={emp.id}>
                    {emp.name} ({emp.email}) — {emp.role}
                  </option>
                ))}
            </select>
          </div>

          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={deleteSource}
              onChange={(e) => setDeleteSource(e.target.checked)}
              className="rounded border-gray-300"
            />
            <span className="text-sm text-gray-700">
              Permanently delete source employee after transfer
            </span>
          </label>
          <p className="text-xs text-gray-400 ml-5 -mt-2">
            If unchecked, the source employee will be deactivated instead.
          </p>
        </div>

        {sourceId && targetId && sourceEmp && targetEmp && (
          <div className="mt-4 bg-amber-50 border border-amber-200 rounded-lg p-3 text-sm text-amber-800">
            Transferring all records from <strong>{sourceEmp.name}</strong> to <strong>{targetEmp.name}</strong>.
            {deleteSource
              ? ' Source employee will be permanently deleted.'
              : ' Source employee will be deactivated.'}
          </div>
        )}

        <button
          onClick={() => setShowConfirm(true)}
          disabled={!sourceId || !targetId || sourceId === targetId || loading}
          className="mt-6 bg-admin-primary hover:bg-admin-primary/90 text-white text-sm font-medium px-6 py-2.5 rounded-lg disabled:opacity-50 transition-colors"
        >
          {loading ? 'Transferring...' : 'Transfer All Data'}
        </button>

        {error && (
          <div className="mt-4 bg-red-50 text-red-700 text-sm px-4 py-2 rounded-md">{error}</div>
        )}

        {result && (
          <div className="mt-6 bg-green-50 border border-green-200 rounded-lg p-4">
            <h3 className="font-medium text-green-800 mb-2">
              Transfer Complete — {result.totalTransferred} records transferred
            </h3>
            <p className="text-sm text-green-700 mb-2">
              From: <strong>{result.sourceEmployeeName}</strong> → To: <strong>{result.targetEmployeeName}</strong>
            </p>
            {result.sourceDeleted && (
              <p className="text-sm text-amber-700">Source employee was permanently deleted.</p>
            )}
            <table className="text-sm text-green-700 w-full max-w-md mt-2">
              <tbody>
                {Object.entries(result.transferredCounts).map(([key, count]) => (
                  <tr key={key} className="border-b border-green-100 last:border-0">
                    <td className="py-1 capitalize">{key.replace(/([A-Z])/g, ' $1').trim()}</td>
                    <td className="py-1 text-right font-mono">{count as number}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p className="text-xs text-green-600 mt-3">Page will refresh in a moment...</p>
          </div>
        )}
      </div>

      <DeleteConfirmModal
        open={showConfirm}
        title="Transfer All Data?"
        message={`This will transfer ALL records from "${sourceEmp?.name}" to "${targetEmp?.name}". ${deleteSource ? 'The source employee will be permanently deleted afterwards.' : 'The source employee will be deactivated afterwards.'} This action CANNOT be undone.`}
        count={-1}
        confirmLabel="Transfer Data"
        onConfirm={handleTransfer}
        onCancel={() => setShowConfirm(false)}
      />
    </div>
  )
}
