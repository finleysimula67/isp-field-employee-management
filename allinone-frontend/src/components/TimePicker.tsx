interface TimePickerProps {
  label: string
  value: string
  onChange: (value: string) => void
}

function to12Hour(hh: number): { hour: number; period: 'AM' | 'PM' } {
  if (hh === 0) return { hour: 12, period: 'AM' }
  if (hh < 12) return { hour: hh, period: 'AM' }
  if (hh === 12) return { hour: 12, period: 'PM' }
  return { hour: hh - 12, period: 'PM' }
}

function to24Hour(hour: number, period: 'AM' | 'PM'): number {
  if (period === 'AM') return hour === 12 ? 0 : hour
  return hour === 12 ? 12 : hour + 12
}

const hours = Array.from({ length: 12 }, (_, i) => i + 1)
const minutes = Array.from({ length: 60 }, (_, i) => String(i).padStart(2, '0'))

export default function TimePicker({ label, value, onChange }: TimePickerProps) {
  const parts = value ? value.split(':') : []
  const hh = parts.length === 2 ? parseInt(parts[0], 10) : NaN
  const mm = parts.length === 2 ? parts[1] : '00'
  const { hour: h12, period } = hh >= 0 && hh <= 23 ? to12Hour(hh) : { hour: 12, period: 'AM' as const }

  const handleChange = (h: number, m: string, p: 'AM' | 'PM') => {
    const h24 = to24Hour(h, p)
    onChange(`${String(h24).padStart(2, '0')}:${m}`)
  }

  return (
    <div>
      <label className="text-xs font-medium text-gray-500 block mb-1">{label}</label>
      <div className="flex gap-1 items-center">
        <select
          value={h12}
          onChange={e => handleChange(Number(e.target.value), mm, period)}
          className="input-field w-[72px] text-center"
        >
          {hours.map(h => (
            <option key={h} value={h}>{h}</option>
          ))}
        </select>
        <span className="text-gray-400 font-medium">:</span>
        <select
          value={mm}
          onChange={e => handleChange(h12, e.target.value, period)}
          className="input-field w-[72px] text-center"
        >
          {minutes.map(m => (
            <option key={m} value={m}>{m}</option>
          ))}
        </select>
        <select
          value={period}
          onChange={e => handleChange(h12, mm, e.target.value as 'AM' | 'PM')}
          className="input-field w-[68px] text-center"
        >
          <option value="AM">AM</option>
          <option value="PM">PM</option>
        </select>
      </div>
    </div>
  )
}
