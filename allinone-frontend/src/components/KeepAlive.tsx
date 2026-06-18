import { useEffect } from 'react'
import client from '../api/client'

export default function KeepAlive() {
  useEffect(() => {
    const interval = setInterval(() => {
      client.get('/auth/check-email?email=ping', { timeout: 5000 }).catch(() => {})
    }, 600000)
    return () => clearInterval(interval)
  }, [])
  return null
}
