interface BatchToolbarProps {
  selectedCount: number
  totalCount: number
  onSelectAll: () => void
  onDeselectAll: () => void
  onDeleteSelected: () => void
  deleteLabel?: string
}

export default function BatchToolbar({ selectedCount, totalCount, onSelectAll, onDeselectAll, onDeleteSelected, deleteLabel }: BatchToolbarProps) {
  if (selectedCount === 0 && totalCount === 0) return null

  return (
    <div className="flex items-center gap-3 px-4 py-2 bg-gray-50 border rounded-lg mb-3">
      <label className="flex items-center gap-2 text-sm text-gray-700">
        <input
          type="checkbox"
          className="rounded border-gray-300"
          checked={selectedCount > 0 && selectedCount === totalCount}
          onChange={selectedCount === totalCount ? onDeselectAll : onSelectAll}
        />
        {selectedCount > 0 ? `${selectedCount} selected` : `Select all (${totalCount})`}
      </label>
      {selectedCount > 0 && (
        <button onClick={onDeleteSelected}
          className="ml-auto text-sm font-medium text-red-600 hover:text-red-800 px-3 py-1 rounded hover:bg-red-50">
          {deleteLabel || 'Delete Selected'}
        </button>
      )}
    </div>
  )
}
