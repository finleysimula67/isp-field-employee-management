import { useEffect, useState } from 'react'
import { getAuditLogs } from '../../api/auditLogs'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

const entityTypes = ['', 'Employee', 'DailyLog', 'LeaveRequest', 'Task', 'PayrollRecord', 'SalaryAdvance', 'Branch', 'Holiday']

export default function AuditLogsPage() {
  const [logs, setLogs] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [entityType, setEntityType] = useState('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  const fetchData = () => {
    setLoading(true)
    getAuditLogs(entityType || undefined, from ? new Date(from).toISOString() : undefined, to ? new Date(to + 'T23:59:59').toISOString() : undefined)
      .then(res => { setLogs(res.data); setLoading(false) })
      .catch(() => { setToast({ message: 'Failed to load audit logs', type: 'error' }); setLoading(false) })
  }

  useEffect(() => { fetchData() }, [])

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Audit Logs</h1>
      </div>
      <div className="card p-4 mb-6">
        <div className="flex flex-wrap gap-4 items-end">
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Entity Type</label>
            <select value={entityType} onChange={e => setEntityType(e.target.value)} className="input-field">
              <option value="">All</option>
              {entityTypes.filter(Boolean).map(t => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">From</label>
            <input type="date" value={from} onChange={e => setFrom(e.target.value)} className="input-field" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">To</label>
            <input type="date" value={to} onChange={e => setTo(e.target.value)} className="input-field" />
          </div>
          <button onClick={fetchData} className="btn-admin">Filter</button>
        </div>
      </div>
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100">
                <th className="text-left py-3 px-4 font-medium text-gray-500">Time</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Actor</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Action</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Entity</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Entity ID</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">From</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">To</th>
                <th className="text-left py-3 px-4 font-medium text-gray-500">Metadata</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <Skeleton variant="table-row" count={5} />
              ) : logs.length === 0 ? (
                <tr><td colSpan={8} className="py-8 text-center text-gray-400">No logs found</td></tr>
              ) : logs.map(log => (
                <tr key={log.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="py-3 px-4 text-xs whitespace-nowrap">{new Date(log.createdAt).toLocaleString()}</td>
                  <td className="py-3 px-4">{log.actor?.name || 'System'}</td>
                  <td className="py-3 px-4"><span className="badge badge-info">{log.action}</span></td>
                  <td className="py-3 px-4">{log.entityType}</td>
                  <td className="py-3 px-4">{log.entityId}</td>
                  <td className="py-3 px-4 text-xs">{log.previousStatus || '-'}</td>
                  <td className="py-3 px-4 text-xs">{log.newStatus || '-'}</td>
                  <td className="py-3 px-4 text-xs max-w-[200px] truncate">{log.metadata || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
