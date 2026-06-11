import client from './client'

export async function generateReport(data: {
  startDate: string
  endDate: string
  employeeId?: number
  branchId?: number
  format?: string
}) {
  const res = await client.post('/reports/generate', { ...data, format: data.format || 'csv' })
  return res.data
}

export async function exportReport(data: {
  startDate: string
  endDate: string
  employeeId?: number
  branchId?: number
  format: string
}) {
  const res = await client.post('/reports/export', { ...data, format: data.format || 'csv' }, {
    responseType: 'blob',
  })
  const url = window.URL.createObjectURL(new Blob([res.data]))
  const link = document.createElement('a')
  link.href = url
  const ext = data.format === 'pdf' ? 'pdf' : data.format === 'excel' ? 'xlsx' : 'csv'
  link.setAttribute('download', `report.${ext}`)
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
