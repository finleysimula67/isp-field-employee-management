import { describe, it, expect, beforeEach, vi } from 'vitest'
import axios from 'axios'

vi.mock('axios', () => {
  const mockAxios = {
    create: vi.fn(() => ({
      defaults: { baseURL: '/api', headers: { 'Content-Type': 'application/json' } },
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() },
      },
    })),
  }
  return { default: mockAxios }
})

describe('API client', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('creates axios instance with /api baseURL', async () => {
    const { default: client } = await import('../../api/client')
    const created = axios.create as ReturnType<typeof vi.fn>
    expect(created).toHaveBeenCalledWith(
      expect.objectContaining({ baseURL: '/api' })
    )
  })

  it('sets Content-Type header', async () => {
    const { default: client } = await import('../../api/client')
    const created = axios.create as ReturnType<typeof vi.fn>
    expect(created).toHaveBeenCalledWith(
      expect.objectContaining({ headers: { 'Content-Type': 'application/json' } })
    )
  })
})
