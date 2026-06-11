import client from './client'

export async function getPayrollRecords(params?: { employeeId?: number; periodLabel?: string }) {
  const res = await client.get('/payroll', { params })
  return res.data
}

export async function getMyPayrollRecords() {
  const res = await client.get('/payroll/my')
  return res.data
}

export async function calculatePayroll(data: any) {
  const res = await client.post('/payroll/calculate', data)
  return res.data
}

export async function approvePayroll(id: number) {
  const res = await client.put(`/payroll/${id}/approve`)
  return res.data
}

export async function markPaid(id: number) {
  const res = await client.put(`/payroll/${id}/pay`)
  return res.data
}

export async function batchCalculatePayroll(data: { periodStart: string; periodEnd: string; employeeIds?: number[] }) {
  const res = await client.post('/payroll/batch-calculate', data)
  return res.data
}
