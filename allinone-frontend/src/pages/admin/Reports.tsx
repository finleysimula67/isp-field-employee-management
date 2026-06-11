import { useEffect, useState } from 'react'
import { getEmployees } from '../../api/employees'
import { getBranches } from '../../api/branches'
import { generateReport, exportReport } from '../../api/reports'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

export default function ReportsPage() {
  const [employees, setEmployees] = useState<any[]>([])
  const [branches, setBranches] = useState<any[]>([])
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [selectedEmployee, setSelectedEmployee] = useState('')
  const [selectedBranch, setSelectedBranch] = useState('')
  const [format, setFormat] = useState('CSV')
  const [report, setReport] = useState<any>(null)
  const [generating, setGenerating] = useState(false)
  const [exporting, setExporting] = useState(false)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  useEffect(() => {
    Promise.all([getEmployees(), getBranches()])
      .then(([empRes, branchRes]) => {
        setEmployees(empRes.data)
        setBranches(branchRes.data)
      })
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
  }, [])

  const handleGenerate = async () => {
    if (!startDate || !endDate) return
    setGenerating(true)
    setToast(null)
    try {
      const payload: any = {
        startDate,
        endDate,
        format: format.toLowerCase(),
      }
      if (selectedEmployee) payload.employeeId = Number(selectedEmployee)
      if (selectedBranch) payload.branchId = Number(selectedBranch)
      const res = await generateReport(payload)
      setReport(res.data)
    } catch {
      setToast({ message: 'Failed to generate report', type: 'error' })
    } finally {
      setGenerating(false)
    }
  }

  const handleExport = async () => {
    setExporting(true)
    setToast(null)
    try {
      const payload: any = {
        startDate,
        endDate,
        format: format.toLowerCase(),
      }
      if (selectedEmployee) payload.employeeId = Number(selectedEmployee)
      if (selectedBranch) payload.branchId = Number(selectedBranch)
      await exportReport(payload)
    } catch {
      setToast({ message: 'Failed to export report', type: 'error' })
    } finally {
      setExporting(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Reports</h1>
      </div>
      <div className="card p-6 mb-6">
        <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Generate Report</h2>
        <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Start Date</label>
            <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} className="input-field w-full" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">End Date</label>
            <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} className="input-field w-full" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Employee (optional)</label>
            <select value={selectedEmployee} onChange={e => setSelectedEmployee(e.target.value)} className="input-field w-full">
              <option value="">All Employees</option>
              {employees.map((emp: any) => (
                <option key={emp.id} value={emp.id}>{emp.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Branch (optional)</label>
            <select value={selectedBranch} onChange={e => setSelectedBranch(e.target.value)} className="input-field w-full">
              <option value="">All Branches</option>
              {branches.map((b: any) => (
                <option key={b.id} value={b.id}>{b.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Format</label>
            <div className="flex gap-4 mt-1">
              {['CSV', 'PDF', 'EXCEL'].map(f => (
                <label key={f} className={`flex items-center gap-2 text-sm px-3 py-1 rounded-full cursor-pointer transition-colors ${format === f ? 'bg-blue-100 text-blue-700 font-medium' : 'text-gray-500 hover:text-gray-700'}`}>
                  <input type="radio" name="format" value={f} checked={format === f} onChange={e => setFormat(e.target.value)} className="sr-only" />
                  {f}
                </label>
              ))}
            </div>
          </div>
        </div>
        <div className="flex gap-3 mt-4">
          <button onClick={handleGenerate} disabled={generating} className="btn-admin">
            {generating ? 'Generating...' : 'Generate Report'}
          </button>
          {report && (
            <button onClick={handleExport} disabled={exporting} className="btn-primary">
              {exporting ? 'Exporting...' : `Export as ${format}`}
            </button>
          )}
        </div>
      </div>

      {report && (
        <>
          <div className="card p-6 mb-6">
            <h3 className="font-display text-lg font-bold text-gray-900 mb-4">Summary</h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-blue-50 p-4 rounded-lg">
                <div className="text-2xl font-bold text-blue-700">{report.summary?.totalDays ?? 0}</div>
                <div className="text-xs text-blue-600">Total Days Worked</div>
              </div>
              <div className="bg-green-50 p-4 rounded-lg">
                <div className="text-2xl font-bold text-green-700">{report.summary?.totalHours ?? 0}</div>
                <div className="text-xs text-green-600">Total Hours</div>
              </div>
              <div className="bg-purple-50 p-4 rounded-lg">
                <div className="text-2xl font-bold text-purple-700">{report.summary?.employeeCount ?? 0}</div>
                <div className="text-xs text-purple-600">Employees</div>
              </div>
              <div className="bg-amber-50 p-4 rounded-lg">
                <div className="text-sm font-bold text-amber-700">{report.summary?.startDate} → {report.summary?.endDate}</div>
                <div className="text-xs text-amber-600">Report Period</div>
              </div>
            </div>
          </div>

          {report.details?.length > 0 && (
            <div className="card overflow-hidden">
              <div className="px-4 py-3 border-b border-gray-100 flex items-center justify-between">
                <h3 className="font-display font-bold text-gray-900">Details ({report.details.length} entries)</h3>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-100">
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Employee</th>
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Branch</th>
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Date</th>
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Category</th>
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Hours</th>
                      <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {report.details.map((row: any) => (
                      <tr key={row.id} className="border-b border-gray-50 hover:bg-gray-50">
                        <td className="py-3 px-4 font-medium">{row.employeeName}</td>
                        <td className="py-3 px-4 text-gray-500">{row.branch || '—'}</td>
                        <td className="py-3 px-4 text-gray-500">{row.date}</td>
                        <td className="py-3 px-4 text-gray-500">{row.category?.replace(/_/g, ' ')}</td>
                        <td className="py-3 px-4 text-gray-500">{row.hoursWorked ?? '—'}</td>
                        <td className="py-3 px-4">
                          <span className="text-xs px-2 py-1 rounded-full bg-green-100 text-green-700">{row.status}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
