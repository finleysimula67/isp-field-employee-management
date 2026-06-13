import { useEffect, useState } from 'react'
import { getEmployees, approveEmployee, createEmployee, updateEmployee, transferOwnership } from '../../api/employees'
import { getBranches } from '../../api/branches'
import { useAuth } from '../../contexts/AuthContext'
import type { Employee, Branch } from '../../types'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'

type Filter = 'all' | 'pending' | 'approved'

const defaultForm = {
  email: '', name: '', phone: '', password: '', role: 'FIELD_EMPLOYEE', branchId: '',
  authType: 'LOCAL_ONLY', wageType: 'DAILY', dailyRate: '', hourlyWage: '',
  totalLeaveDaysPerYear: '', remainingLeaveDays: '', carryOverLeave: '', maxAdvanceLimit: '',
}

export default function EmployeesPage() {
  const [employees, setEmployees] = useState<Employee[]>([])
  const [branches, setBranches] = useState<Branch[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<Filter>('all')
  const [approving, setApproving] = useState<number | null>(null)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState(defaultForm)
  const [submitting, setSubmitting] = useState(false)
  const [showTransfer, setShowTransfer] = useState(false)
  const [transferTarget, setTransferTarget] = useState('')
  const [transferring, setTransferring] = useState(false)
  const [editingEmployee, setEditingEmployee] = useState<Employee | null>(null)
  const { user } = useAuth()

  const fetchAll = () => {
    setLoading(true)
    Promise.all([getEmployees(), getBranches()])
      .then(([eRes, bRes]) => {
        setEmployees(eRes.data || [])
        setBranches(bRes.data || [])
        setLoading(false)
      })
      .catch(() => { setToast({ message: 'Failed to load data', type: 'error' }); setLoading(false) })
  }

  useEffect(() => { fetchAll() }, [])

  const handleApprove = async (id: number) => {
    setApproving(id)
    try {
      await approveEmployee(id)
      setEmployees(employees.map(e => e.id === id ? { ...e, isAccountApproved: true } : e))
    } catch {}
    setApproving(null)
  }

  const resetForm = () => {
    setForm(defaultForm)
    setEditingEmployee(null)
    setShowForm(false)
    setToast(null)
  }

  const handleEdit = (emp: Employee) => {
    setEditingEmployee(emp)
    setForm({
      email: emp.email,
      name: emp.name,
      phone: emp.phone?.toString() || '',
      password: '',
      role: emp.role,
      branchId: emp.branchId?.toString() || '',
      authType: emp.authType,
      wageType: emp.wageType || 'DAILY',
      dailyRate: emp.dailyRate?.toString() || '',
      hourlyWage: emp.hourlyWage?.toString() || '',
      totalLeaveDaysPerYear: emp.totalLeaveDaysPerYear?.toString() || '',
      remainingLeaveDays: emp.remainingLeaveDays?.toString() || '',
      carryOverLeave: emp.carryOverLeave?.toString() || '',
      maxAdvanceLimit: emp.maxAdvanceLimit?.toString() || '',
    })
    setShowForm(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.email || !form.name) { setToast({ message: 'Email and Name are required', type: 'error' }); return }
    if (!editingEmployee && form.authType === 'LOCAL_ONLY' && !form.password) { setToast({ message: 'Password is required for local auth', type: 'error' }); return }
    setSubmitting(true)
    setToast(null)
    try {
      if (editingEmployee) {
        await updateEmployee(editingEmployee.id, {
          name: form.name.trim(),
          phone: form.phone.trim() || null,
          role: form.role,
          branchId: form.branchId ? Number(form.branchId) : null,
          wageType: form.wageType || undefined,
          dailyRate: form.dailyRate ? Number(form.dailyRate) : null,
          hourlyWage: form.hourlyWage ? Number(form.hourlyWage) : null,
          totalLeaveDaysPerYear: form.totalLeaveDaysPerYear ? Number(form.totalLeaveDaysPerYear) : null,
          remainingLeaveDays: form.remainingLeaveDays ? Number(form.remainingLeaveDays) : null,
          carryOverLeave: form.carryOverLeave ? Number(form.carryOverLeave) : null,
          maxAdvanceLimit: form.maxAdvanceLimit ? Number(form.maxAdvanceLimit) : null,
        })
        setToast({ message: 'Employee updated', type: 'success' })
      } else {
        await createEmployee({
          email: form.email.trim(),
          name: form.name.trim(),
          phone: form.phone.trim() || null,
          password: form.password || undefined,
          role: form.role,
          branchId: form.branchId ? Number(form.branchId) : null,
          authType: form.authType,
          wageType: form.wageType || undefined,
          dailyRate: form.dailyRate ? Number(form.dailyRate) : null,
          hourlyWage: form.hourlyWage ? Number(form.hourlyWage) : null,
          totalLeaveDaysPerYear: form.totalLeaveDaysPerYear ? Number(form.totalLeaveDaysPerYear) : null,
        })
        setToast({ message: 'Employee created', type: 'success' })
      }
      resetForm()
      fetchAll()
    } catch { setToast({ message: editingEmployee ? 'Failed to update employee' : 'Failed to create employee', type: 'error' }) }
    finally { setSubmitting(false) }
  }

  const handleTransfer = async () => {
    if (!transferTarget) { setToast({ message: 'Select a target employee', type: 'error' }); return }
    if (!window.confirm('Transfer ownership to this employee? They will become the new owner and be promoted to SUPER_ADMIN if not already.')) return
    setTransferring(true)
    setToast(null)
    try {
      await transferOwnership(Number(transferTarget))
      setToast({ message: 'Ownership transferred successfully', type: 'success' })
      setShowTransfer(false)
      setTransferTarget('')
      fetchAll()
    } catch { setToast({ message: 'Failed to transfer ownership', type: 'error' }) }
    finally { setTransferring(false) }
  }

  const currentOwner = employees.find(e => e.isOwner)

  const filtered = employees.filter(e => {
    if (filter === 'pending') return !e.isAccountApproved
    if (filter === 'approved') return e.isAccountApproved
    return true
  })

  const tabs: { key: Filter; label: string; count: number }[] = [
    { key: 'all', label: 'All', count: employees.length },
    { key: 'pending', label: 'Pending', count: employees.filter(e => !e.isAccountApproved).length },
    { key: 'approved', label: 'Approved', count: employees.filter(e => e.isAccountApproved).length },
  ]

  return (
    <div>
      <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Employees</h1>
        <div className="flex gap-2">
          {currentOwner && currentOwner.id === user?.id && (
            <button onClick={() => setShowTransfer(true)} className="btn-secondary">Transfer Ownership</button>
          )}
          <button onClick={() => { resetForm(); setShowForm(true) }} className="btn-admin">+ Add Employee</button>
        </div>
      </div>

      {showTransfer && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={e => { if (e.target === e.currentTarget) { setShowTransfer(false); setTransferTarget('') } }}>
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6" onClick={e => e.stopPropagation()}>
            <h2 className="font-display text-lg font-bold text-gray-900 mb-4">Transfer Ownership</h2>
            <p className="text-sm text-gray-500 mb-4">Select an approved active employee to become the new owner. They will be promoted to SUPER_ADMIN if not already.</p>
            <div className="space-y-4">
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">New Owner</label>
                <select value={transferTarget} onChange={e => setTransferTarget(e.target.value)} className="input-field w-full">
                  <option value="">Select employee...</option>
                  {employees.filter(e => e.isAccountApproved && e.isActive && e.id !== currentOwner?.id).map(e => (
                    <option key={e.id} value={e.id}>{e.name} ({e.email}) — {e.role.replace('_', ' ')}</option>
                  ))}
                </select>
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => { setShowTransfer(false); setTransferTarget('') }} className="btn-secondary">Cancel</button>
                <button type="button" onClick={handleTransfer} disabled={transferring || !transferTarget} className="btn-admin">{transferring ? 'Transferring...' : 'Transfer Ownership'}</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {showForm && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={e => { if (e.target === e.currentTarget) resetForm() }}>
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
            <h2 className="font-display text-lg font-bold text-gray-900 mb-4">{editingEmployee ? 'Edit Employee' : 'Add Employee'}</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Email *</label>
                  <input type="email" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))} className="input-field w-full" required />
                </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Name *</label>
                <input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} className="input-field w-full" required />
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Phone</label>
                <input type="tel" value={form.phone} onChange={e => setForm(f => ({ ...f, phone: e.target.value }))} className="input-field w-full" placeholder="98XXXXXXXX" />
              </div>
            </div>
            <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Role</label>
                <select value={form.role} onChange={e => setForm(f => ({ ...f, role: e.target.value }))} className="input-field w-full">
                  <option value="FIELD_EMPLOYEE">Field Employee</option>
                  <option value="BRANCH_MANAGER">Branch Manager</option>
                  <option value="SUPER_ADMIN">Super Admin</option>
                </select>
              </div>
              <div>
                <label className="text-xs font-medium text-gray-500 block mb-1">Branch</label>
                <select value={form.branchId} onChange={e => setForm(f => ({ ...f, branchId: e.target.value }))} className="input-field w-full">
                  <option value="">None</option>
                  {branches.map(b => (
                    <option key={b.id} value={b.id}>{b.name}</option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Auth Type</label>
                  <select value={form.authType} onChange={e => setForm(f => ({ ...f, authType: e.target.value }))} className="input-field w-full">
                    <option value="LOCAL_ONLY">Local (Email/Password)</option>
                    <option value="GOOGLE_ONLY">Google Only</option>
                    <option value="LOCAL_AND_GOOGLE">Local + Google</option>
                  </select>
                </div>
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Wage Type</label>
                  <select value={form.wageType} onChange={e => setForm(f => ({ ...f, wageType: e.target.value }))} className="input-field w-full">
                    <option value="DAILY">Daily Rate</option>
                    <option value="HOURLY">Hourly Wage</option>
                  </select>
                </div>
              </div>
              {form.authType !== 'GOOGLE_ONLY' && (
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Password {form.authType === 'LOCAL_ONLY' ? '*' : ''}</label>
                  <input type="password" value={form.password} onChange={e => setForm(f => ({ ...f, password: e.target.value }))} className="input-field w-full" required={form.authType === 'LOCAL_ONLY'} />
                </div>
              )}
              <div className="grid grid-cols-2 gap-4">
                {form.wageType === 'DAILY' ? (
                  <div>
                    <label className="text-xs font-medium text-gray-500 block mb-1">Daily Rate (Rs.)</label>
                    <input type="number" value={form.dailyRate} onChange={e => setForm(f => ({ ...f, dailyRate: e.target.value }))} className="input-field w-full" placeholder="800" min="0" />
                  </div>
                ) : (
                  <div>
                    <label className="text-xs font-medium text-gray-500 block mb-1">Hourly Wage (Rs.)</label>
                    <input type="number" value={form.hourlyWage} onChange={e => setForm(f => ({ ...f, hourlyWage: e.target.value }))} className="input-field w-full" placeholder="100" min="0" />
                  </div>
                )}
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Leave Days/Year</label>
                  <input type="number" value={form.totalLeaveDaysPerYear} onChange={e => setForm(f => ({ ...f, totalLeaveDaysPerYear: e.target.value }))} className="input-field w-full" placeholder="0" min="0" />
                </div>
              </div>
              {editingEmployee && (
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-xs font-medium text-gray-500 block mb-1">Remaining Leave Days</label>
                    <input type="number" value={form.remainingLeaveDays} onChange={e => setForm(f => ({ ...f, remainingLeaveDays: e.target.value }))} className="input-field w-full" placeholder="0" min="0" />
                  </div>
                  <div>
                    <label className="text-xs font-medium text-gray-500 block mb-1">Carry Over Leave</label>
                    <input type="number" value={form.carryOverLeave} onChange={e => setForm(f => ({ ...f, carryOverLeave: e.target.value }))} className="input-field w-full" placeholder="0" min="0" />
                  </div>
                </div>
              )}
              {editingEmployee && (
                <div>
                  <label className="text-xs font-medium text-gray-500 block mb-1">Max Advance Limit (Rs.)</label>
                  <input type="number" value={form.maxAdvanceLimit} onChange={e => setForm(f => ({ ...f, maxAdvanceLimit: e.target.value }))} className="input-field w-full" placeholder="5000" min="0" />
                </div>
              )}
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={resetForm} className="btn-secondary">Cancel</button>
                <button type="submit" disabled={submitting} className="btn-admin">{submitting ? 'Saving...' : editingEmployee ? 'Save Changes' : 'Create Employee'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="flex gap-1 mb-4 bg-gray-100 rounded-lg p-1 w-fit">
        {tabs.map(tab => (
          <button
            key={tab.key}
            onClick={() => setFilter(tab.key)}
            className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors ${
              filter === tab.key ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab.label} ({tab.count})
          </button>
        ))}
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100">
              <th className="text-left py-3 px-4 font-medium text-gray-500">Name</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Phone</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Email</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Role</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Leave</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Branch</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500"></th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <Skeleton variant="table-row" count={5} />
            ) : filtered.length === 0 ? (
              <tr><td colSpan={8} className="py-8 text-center text-gray-400">Nothing here yet</td></tr>
            ) : filtered.map((emp) => (
              <tr key={emp.id} className="border-b border-gray-50 hover:bg-gray-50">
                <td className="py-3 px-4 font-medium">
                  {emp.name}
                  {emp.isOwner && <span className="ml-2 text-xs px-1.5 py-0.5 rounded-full bg-yellow-100 text-yellow-700 font-medium">Owner</span>}
                </td>
                <td className="py-3 px-4 text-gray-500">{emp.phone || '—'}</td>
                <td className="py-3 px-4 text-gray-500">{emp.email}</td>
                <td className="py-3 px-4">
                  <span className="text-xs px-2 py-1 rounded-full bg-gray-100 text-gray-700">
                    {emp.role.replace('_', ' ')}
                  </span>
                </td>
                <td className="py-3 px-4 text-gray-500 text-xs">{emp.remainingLeaveDays ?? '—'}</td>
                <td className="py-3 px-4 text-gray-500">{emp.branchName || '—'}</td>
                <td className="py-3 px-4">
                  {!emp.isAccountApproved ? (
                    <span className="badge-pending">Pending</span>
                  ) : emp.isActive ? (
                    <span className="badge-approved">Active</span>
                  ) : (
                    <span className="badge-rejected">Inactive</span>
                  )}
                </td>
                <td className="py-3 px-4">
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleEdit(emp)}
                      className="text-xs font-medium text-gray-500 hover:text-gray-700"
                    >Edit</button>
                    {!emp.isAccountApproved && (
                      <button
                        onClick={() => handleApprove(emp.id)}
                        disabled={approving === emp.id}
                        className="text-xs font-medium text-brand-600 hover:text-brand-700 disabled:opacity-50"
                      >
                        {approving === emp.id ? 'Approving...' : 'Approve'}
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
