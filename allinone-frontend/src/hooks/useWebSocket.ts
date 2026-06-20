import { useEffect, useRef, useCallback } from 'react'
import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuth } from '../contexts/AuthContext'

const WS_URL = import.meta.env.VITE_WS_URL || 'https://allinone-backend-xoh0.onrender.com/ws'

interface UseWebSocketOptions {
  onNotification?: (data: any) => void
  onCountUpdate?: (count: number) => void
}

export function useWebSocket({ onNotification, onCountUpdate }: UseWebSocketOptions) {
  const { token, user, isAuthenticated } = useAuth()
  const clientRef = useRef<Client | null>(null)
  const retriesRef = useRef(0)
  const maxRetries = 5

  const connect = useCallback(() => {
    if (!token || !user) return

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        retriesRef.current = 0
        client.subscribe(`/topic/notifications/${user.id}`, (message: IMessage) => {
          try {
            const data = JSON.parse(message.body)
            onNotification?.(data)
          } catch {}
        })
        client.subscribe(`/topic/notifications/${user.id}/count`, (message: IMessage) => {
          try {
            const count = JSON.parse(message.body)
            onCountUpdate?.(count)
          } catch {}
        })
      },
      onStompError: () => {
        retriesRef.current++
        if (retriesRef.current >= maxRetries) {
          client.deactivate()
        }
      },
      onWebSocketClose: () => {
        if (retriesRef.current >= maxRetries) {
          client.deactivate()
        }
      },
    })

    client.activate()
    clientRef.current = client
  }, [token, user, onNotification, onCountUpdate])

  const disconnect = useCallback(() => {
    clientRef.current?.deactivate()
    clientRef.current = null
  }, [])

  useEffect(() => {
    if (isAuthenticated && token && user) {
      connect()
    }
    return () => disconnect()
  }, [isAuthenticated, token, user, connect, disconnect])

  return { disconnect, reconnect: connect }
}
