import { useEffect } from 'react'
import client from '../api/client'

export default function KeepAlive() {
  useEffect(() => {
    const ping = () => {
      client.get('/auth/check-email?email=ping', { timeout: 8000 }).catch(() => {})
    }
    ping()
    const interval = setInterval(ping, 120000)
    return () => clearInterval(interval)
  }, [])
  return null
}
