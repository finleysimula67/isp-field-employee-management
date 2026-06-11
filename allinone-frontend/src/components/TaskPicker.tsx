interface Task {
  id: number
  title: string
  description?: string
  priority?: string
  status?: string
  scheduledDate?: string
  customerName?: string
  customerPhone?: string
  customerAddress?: string
}

interface TaskPickerProps {
  tasks: Task[]
  value: string
  onChange: (taskId: string) => void
}

const priorityColors: Record<string, string> = {
  LOW: 'bg-gray-100 text-gray-700',
  MEDIUM: 'bg-blue-100 text-blue-700',
  HIGH: 'bg-orange-100 text-orange-700',
  URGENT: 'bg-red-100 text-red-700',
}

export default function TaskPicker({ tasks, value, onChange }: TaskPickerProps) {
  const selectedTask = tasks.find(t => String(t.id) === value)

  return (
    <div>
      <label className="text-xs font-medium text-gray-500 block mb-1">Assigned Task</label>
      <select
        value={value}
        onChange={e => onChange(e.target.value)}
        className="input-field w-full"
      >
        <option value="">None — general log</option>
        {tasks.map(t => (
          <option key={t.id} value={t.id}>
            {t.title}{t.customerName ? ` — ${t.customerName}` : ''}
          </option>
        ))}
      </select>
      {selectedTask && (
        <div className="mt-2 bg-gray-50 rounded-lg p-3 border border-gray-100">
          <div className="flex items-start justify-between mb-1">
            <span className="text-sm font-semibold text-gray-900">{selectedTask.title}</span>
            {selectedTask.priority && (
              <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${priorityColors[selectedTask.priority] || 'bg-gray-100 text-gray-700'}`}>
                {selectedTask.priority}
              </span>
            )}
          </div>
          {selectedTask.description && (
            <p className="text-xs text-gray-500 mb-2">{selectedTask.description}</p>
          )}
          {selectedTask.customerName && (
            <div className="text-xs text-gray-600 space-y-0.5">
              <p><span className="font-medium">Customer:</span> {selectedTask.customerName}</p>
              {selectedTask.customerPhone && <p><span className="font-medium">Phone:</span> {selectedTask.customerPhone}</p>}
              {selectedTask.customerAddress && <p><span className="font-medium">Address:</span> {selectedTask.customerAddress}</p>}
            </div>
          )}
          <div className="flex items-center gap-3 mt-2 text-xs text-gray-400">
            {selectedTask.status && <span>Status: {selectedTask.status.replace(/_/g, ' ')}</span>}
            {selectedTask.scheduledDate && <span>Scheduled: {selectedTask.scheduledDate}</span>}
          </div>
        </div>
      )}
    </div>
  )
}
