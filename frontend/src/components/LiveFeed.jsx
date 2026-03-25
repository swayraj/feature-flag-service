import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

function LiveFeed() {
  const [events, setEvents] = useState([])
  const [connected, setConnected] = useState(false)
  const clientRef = useRef(null)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true)
        client.subscribe('/topic/flags', message => {
          const event = JSON.parse(message.body)
          setEvents(prev => [event, ...prev].slice(0, 20))
        })
      },
      onDisconnect: () => setConnected(false),
    })

    client.activate()
    clientRef.current = client

    return () => client.deactivate()
  }, [])

  return (
    <div className="mt-10 border border-gray-800 rounded-xl p-6 bg-gray-900/50">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-sm font-bold font-mono text-gray-400 uppercase tracking-widest">
          Live Flag Feed
        </h2>
        <div className="flex items-center gap-2">
          <span className={`w-2 h-2 rounded-full ${connected ? 'bg-cyan-400 animate-pulse' : 'bg-gray-600'}`} />
          <span className="text-xs font-mono text-gray-600">
            {connected ? 'connected' : 'connecting...'}
          </span>
        </div>
      </div>

      {events.length === 0 ? (
        <p className="text-xs font-mono text-gray-600">
          Waiting for events — toggle or create a flag to see it appear here.
        </p>
      ) : (
        <div className="flex flex-col gap-2">
          {events.map((event, i) => (
            <EventCard key={i} event={event} />
          ))}
        </div>
      )}
    </div>
  )
}

function EventCard({ event }) {
  const typeColors = {
    FLAG_CREATED: 'text-green-400 border-green-500/20 bg-green-500/5',
    FLAG_UPDATED: 'text-cyan-400 border-cyan-500/20 bg-cyan-500/5',
    FLAG_DELETED: 'text-red-400 border-red-500/20 bg-red-500/5',
    FLAG_TOGGLED: 'text-yellow-400 border-yellow-500/20 bg-yellow-500/5',
  }

  const style = typeColors[event.eventType] || 'text-gray-400 border-gray-700 bg-gray-800/50'
  const time = event.timestamp
    ? new Date(event.timestamp).toLocaleTimeString()
    : ''

  return (
    <div className={`flex items-center justify-between px-3 py-2 rounded-lg border font-mono text-xs ${style}`}>
      <div className="flex items-center gap-3">
        <span className="uppercase tracking-wider opacity-70">{event.eventType?.replace('FLAG_', '')}</span>
        <span className="text-gray-300">{event.flagName ?? '—'}</span>
        {event.enabled !== undefined && (
          <span className={event.enabled ? 'text-cyan-400' : 'text-gray-500'}>
            {event.enabled ? 'enabled' : 'disabled'}
          </span>
        )}
      </div>
      <span className="text-gray-600 text-[10px]">{time}</span>
    </div>
  )
}

export default LiveFeed
