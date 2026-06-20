import { useEffect, useState } from 'react'
import { getTasks, createTask, updateTask, deleteTask, batchDeleteTasks } from '../../api/tasks'
import { getEmployees } from '../../api/employees'
import Toast from '../../components/Toast'
import Skeleton from '../../components/Skeleton'
import DeleteConfirmModal from '../../components/DeleteConfirmModal'

const priorityColors: Record<string, string> = {
  LOW: 'bg-gray-100 text-gray-700',
  MEDIUM: 'bg-blue-100 text-blue-700',
  HIGH: 'bg-orange-100 text-orange-700',
  URGENT: 'bg-red-100 text-red-700',
}

const statusColors: Record<string, string> = {
  OPEN: 'badge-pending',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-800',
  COMPLETED: 'badge-approved',
  CANCELLED: 'bg-gray-100 text-gray-500',
}

const initialForm = {
  assignedTo: '',
  title: '',
  description: '',
  priority: 'MEDIUM',
  scheduledDate: '',
  customerName: '',
  customerPhone: '',
  customerAddress: '',
}

export default function TasksPage() {
  const [tasks, setTasks] = useState<any[]>([])
  const [employees, setEmployees] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [filterStatus, setFilterStatus] = useState('')
  const [form, setForm] = useState(initialForm)
  const [editingTask, setEditingTask] = useState<any | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null)
  const [deleting, setDeleting] = useState(false)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [confirmBatchDelete, setConfirmBatchDelete] = useState(false)

  const fetchData = () => {
    setLoading(true)
    const params: any = {}
    if (filterStatus) params.status = filterStatus
    Promise.all([getTasks(params), getEmployees()])
      .then(([taskRes, empRes]) => {
        setTasks(taskRes.data)
        setEmployees(empRes.data)
      })
      .catch(() => setToast({ message: 'Failed to load data', type: 'error' }))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchData() }, [])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setToast(null)
    try {
      await createTask({
        ...form,
        assignedTo: Number(form.assignedTo),
      })
      setForm(initialForm)
      fetchData()
    } catch {
      setToast({ message: 'Failed to create task', type: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  const handleEdit = (task: any) => {
    setEditingTask(task)
    setForm({
      assignedTo: String(task.assignedTo || ''),
      title: task.title || '',
      description: task.description || '',
      priority: task.priority || 'MEDIUM',
      scheduledDate: task.scheduledDate || '',
      customerName: task.customerName || '',
      customerPhone: task.customerPhone || '',
      customerAddress: task.customerAddress || '',
    })
  }

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!editingTask) return
    setSubmitting(true)
    setToast(null)
    try {
      await updateTask(editingTask.id, {
        assignedTo: Number(form.assignedTo),
        title: form.title,
        description: form.description,
        priority: form.priority,
        scheduledDate: form.scheduledDate || null,
        customerName: form.customerName || null,
        customerPhone: form.customerPhone || null,
        customerAddress: form.customerAddress || null,
      })
      setEditingTask(null)
      setForm(initialForm)
      setToast({ message: 'Task updated', type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Failed to update task', type: 'error' })
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (confirmDeleteId === null) return
    setDeleting(true)
    setDeletingId(confirmDeleteId)
    try {
      await deleteTask(confirmDeleteId)
      setConfirmDeleteId(null)
      setToast({ message: 'Task deleted', type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Failed to delete task', type: 'error' })
    } finally {
      setDeletingId(null)
      setDeleting(false)
    }
  }

  const toggleSelect = (id: number) => {
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id])
  }

  const toggleSelectAll = () => {
    if (selectedIds.length === tasks.length) setSelectedIds([])
    else setSelectedIds(tasks.map(t => t.id))
  }

  const handleBatchDelete = async () => {
    if (selectedIds.length === 0) return
    setDeleting(true)
    try {
      await batchDeleteTasks({ ids: selectedIds })
      setConfirmBatchDelete(false)
      setSelectedIds([])
      setToast({ message: `${selectedIds.length} task(s) deleted`, type: 'success' })
      fetchData()
    } catch {
      setToast({ message: 'Batch delete failed', type: 'error' })
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl font-bold text-gray-900">Tasks</h1>
      </div>
      <div className="card p-6 mb-6">
        <h2 className="font-display text-lg font-bold text-gray-900 mb-4">{editingTask ? 'Edit Task' : 'Create Task'}</h2>
        <Toast message={toast?.message || ''} type={toast?.type || 'info'} visible={!!toast} onClose={() => setToast(null)} />
        <form onSubmit={editingTask ? handleUpdate : handleCreate} className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Assigned To</label>
            <select value={form.assignedTo} onChange={e => setForm(f => ({ ...f, assignedTo: e.target.value }))} className="input-field" required>
              <option value="">Select employee</option>
              {employees.filter(e => e.isActive).map((emp: any) => (
                <option key={emp.id} value={emp.id}>{emp.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Priority</label>
            <select value={form.priority} onChange={e => setForm(f => ({ ...f, priority: e.target.value }))} className="input-field">
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>
          <div className="md:col-span-2">
            <label className="text-xs font-medium text-gray-500 block mb-1">Title</label>
            <input value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))} className="input-field w-full" required />
          </div>
          <div className="md:col-span-2">
            <label className="text-xs font-medium text-gray-500 block mb-1">Description</label>
            <textarea value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} className="input-field w-full" rows={3} />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Scheduled Date</label>
            <input type="date" value={form.scheduledDate} onChange={e => setForm(f => ({ ...f, scheduledDate: e.target.value }))} className="input-field w-full" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Customer Name</label>
            <input value={form.customerName} onChange={e => setForm(f => ({ ...f, customerName: e.target.value }))} className="input-field w-full" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Customer Phone</label>
            <input value={form.customerPhone} onChange={e => setForm(f => ({ ...f, customerPhone: e.target.value }))} className="input-field w-full" />
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Customer Address</label>
            <input value={form.customerAddress} onChange={e => setForm(f => ({ ...f, customerAddress: e.target.value }))} className="input-field w-full" />
          </div>
          <div className="md:col-span-2 flex gap-3">
            <button type="submit" disabled={submitting} className="btn-admin">{submitting ? 'Saving...' : editingTask ? 'Update Task' : 'Create Task'}</button>
            {editingTask && (
              <button type="button" onClick={() => { setEditingTask(null); setForm(initialForm) }} className="btn-secondary">Cancel</button>
            )}
          </div>
        </form>
      </div>
      <div className="card p-4 mb-6">
        <div className="flex gap-4 items-end">
          <div>
            <label className="text-xs font-medium text-gray-500 block mb-1">Status</label>
            <select value={filterStatus} onChange={e => { setFilterStatus(e.target.value); fetchData() }} className="input-field">
              <option value="">All</option>
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
          <button onClick={fetchData} className="btn-admin">Filter</button>
        </div>
      </div>
      {selectedIds.length > 0 && (
        <div className="card p-3 mb-4 flex flex-wrap items-center gap-3 bg-blue-50 border-blue-200">
          <span className="text-sm font-medium text-blue-800">{selectedIds.length} selected</span>
          <button onClick={() => setSelectedIds([])} className="btn-ghost text-xs">Clear</button>
          <button onClick={() => setConfirmBatchDelete(true)} className="text-xs bg-red-50 text-red-600 px-3 py-1.5 rounded-lg hover:bg-red-100 font-medium">Delete Selected</button>
        </div>
      )}
      <div className="card overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100">
              <th className="text-left py-3 px-4 w-10">
                <input type="checkbox" onChange={toggleSelectAll} checked={selectedIds.length === tasks.length && tasks.length > 0} className="rounded" />
              </th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Assigned To</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Title</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Priority</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Status</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Scheduled</th>
              <th className="text-left py-3 px-4 font-medium text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <Skeleton variant="table-row" count={5} />
            ) : tasks.length === 0 ? (
              <tr><td colSpan={7} className="py-8 text-center text-gray-400">No tasks yet.</td></tr>
            ) : tasks.map((task: any) => {
              const emp = employees.find(e => e.id === task.assignedTo)
              return (
                <tr key={task.id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="py-3 px-4">
                    <input type="checkbox" checked={selectedIds.includes(task.id)} onChange={() => toggleSelect(task.id)} className="rounded" />
                  </td>
                  <td className="py-3 px-4 font-medium">{emp?.name || `#${task.assignedTo}`}</td>
                  <td className="py-3 px-4 text-gray-500">{task.title}</td>
                  <td className="py-3 px-4">
                    <span className={`text-xs px-2 py-1 rounded-full font-medium ${priorityColors[task.priority] || 'bg-gray-100 text-gray-700'}`}>
                      {task.priority}
                    </span>
                  </td>
                  <td className="py-3 px-4">
                    <span className={statusColors[task.status] || 'badge-pending'}>{task.status?.replace(/_/g, ' ')}</span>
                  </td>
                  <td className="py-3 px-4 text-gray-500">{task.scheduledDate || '—'}</td>
                  <td className="py-3 px-4">
                    <div className="flex gap-2">
                      <button onClick={() => handleEdit(task)} className="text-xs text-blue-600 hover:text-blue-800">Edit</button>
                      <button onClick={() => setConfirmDeleteId(task.id)} className="text-xs text-red-600 hover:text-red-800">Delete</button>
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
      <DeleteConfirmModal open={confirmDeleteId !== null} title="Delete Task?" message="This task will be soft-deleted and moved to the Recycle Bin. This action can be undone." onConfirm={handleDelete} onCancel={() => setConfirmDeleteId(null)} loading={deleting} />
      <DeleteConfirmModal open={confirmBatchDelete} title={`Delete ${selectedIds.length} Tasks?`} message="These tasks will be soft-deleted and moved to the Recycle Bin." count={selectedIds.length} onConfirm={handleBatchDelete} onCancel={() => setConfirmBatchDelete(false)} loading={deleting} />
    </div>
  )
}