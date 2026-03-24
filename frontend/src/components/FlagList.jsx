import { useEffect, useState } from 'react'

const API_HEADERS = {
  'X-API-Key': 'test-key-123'
}

function FlagList() {
  const [flags, setFlags] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetch('/api/flags', { headers: API_HEADERS })
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then(data => {
        setFlags(data)
        setLoading(false)
      })
      .catch(err => {
        setError(err.message)
        setLoading(false)
      })
  }, [])

  if (loading) return (
    <p className="text-gray-500 font-mono text-sm">Loading flags...</p>
  )

  if (error) return (
    <p className="text-red-400 font-mono text-sm">Error: {error}</p>
  )

  if (flags.length === 0) return (
    <p className="text-gray-500 font-mono text-sm">No flags found.</p>
  )

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {flags.map(flag => (
        <FlagCard key={flag.id} flag={flag} />
      ))}
    </div>
  )
}

function FlagCard({ flag }) {
  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 flex flex-col gap-3 hover:border-cyan-500/40 transition-colors">
      <div className="flex items-start justify-between gap-2">
        <span className="font-mono font-bold text-sm text-gray-100 break-all">
          {flag.name}
        </span>
        {flag.enabled ? (
          <span className="shrink-0 text-[10px] font-bold font-mono uppercase tracking-widest px-2 py-1 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
            Enabled
          </span>
        ) : (
          <span className="shrink-0 text-[10px] font-bold font-mono uppercase tracking-widest px-2 py-1 rounded-full bg-gray-800 text-gray-500 border border-gray-700">
            Disabled
          </span>
        )}
      </div>
      <div className="text-xs text-gray-500 font-mono">
        Rollout: <span className="text-gray-300">{flag.rolloutPercentage ?? 0}%</span>
      </div>
    </div>
  )
}

export default FlagList
