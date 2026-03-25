import { useEffect, useState } from 'react'

const API_HEADERS = {
  'Content-Type': 'application/json',
  'X-API-Key': 'test-key-123',
}

function FlagList({ refreshKey, onFlagsLoaded }) {
  const [flags, setFlags] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  function fetchFlags() {
    fetch('/api/flags', { headers: API_HEADERS })
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then(data => {
        setFlags(data)
        setLoading(false)
        if (onFlagsLoaded) onFlagsLoaded(data)
      })
      .catch(err => {
        setError(err.message)
        setLoading(false)
      })
  }

  useEffect(() => {
    fetchFlags()
  }, [refreshKey])

  if (loading) return <p className="text-gray-500 font-mono text-sm">Loading flags...</p>
  if (error) return <p className="text-red-400 font-mono text-sm">Error: {error}</p>
  if (flags.length === 0) return <p className="text-gray-500 font-mono text-sm">No flags found.</p>

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {flags.map(flag => (
        <FlagCard key={flag.id} flag={flag} onUpdate={fetchFlags} />
      ))}
    </div>
  )
}

function FlagCard({ flag, onUpdate }) {
  const [editMode, setEditMode] = useState(false)
  const [newRollout, setNewRollout] = useState(flag.rolloutPercentage ?? 0)
  const [busy, setBusy] = useState(false)

  function toggle() {
    setBusy(true)
    fetch(`/api/flags/${flag.id}/toggle`, { method: 'POST', headers: API_HEADERS })
      .then(res => { if (!res.ok) throw new Error(`HTTP ${res.status}`); return res.json() })
      .then(() => onUpdate())
      .catch(err => alert(err.message))
      .finally(() => setBusy(false))
  }

  function deleteFlag() {
    if (!confirm(`Delete "${flag.name}"?`)) return
    setBusy(true)
    fetch(`/api/flags/${flag.id}`, { method: 'DELETE', headers: API_HEADERS })
      .then(res => { if (!res.ok) throw new Error(`HTTP ${res.status}`) })
      .then(() => onUpdate())
      .catch(err => alert(err.message))
      .finally(() => setBusy(false))
  }

  function saveRollout() {
    setBusy(true)
    fetch(`/api/flags/${flag.id}`, {
      method: 'PUT',
      headers: API_HEADERS,
      body: JSON.stringify({ name: flag.name, enabled: flag.enabled, rolloutPercentage: Number(newRollout) }),
    })
      .then(res => { if (!res.ok) throw new Error(`HTTP ${res.status}`); return res.json() })
      .then(() => { setEditMode(false); onUpdate() })
      .catch(err => alert(err.message))
      .finally(() => setBusy(false))
  }

  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 flex flex-col gap-3 hover:border-cyan-500/40 transition-colors">
      <div className="flex items-start justify-between gap-2">
        <span className="font-mono font-bold text-sm text-gray-100 break-all">{flag.name}</span>
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

      {editMode ? (
        <div className="flex items-center gap-2">
          <input
            type="number" min="0" max="100"
            value={newRollout}
            onChange={e => setNewRollout(e.target.value)}
            className="w-20 bg-gray-800 border border-gray-600 rounded px-2 py-1 text-sm font-mono text-gray-100 focus:outline-none focus:border-cyan-500"
          />
          <span className="text-xs text-gray-500 font-mono">%</span>
          <button onClick={saveRollout} disabled={busy}
            className="text-xs font-mono px-2 py-1 rounded bg-cyan-600 hover:bg-cyan-500 text-white disabled:opacity-40">
            Save
          </button>
          <button onClick={() => setEditMode(false)}
            className="text-xs font-mono px-2 py-1 rounded bg-gray-700 hover:bg-gray-600 text-gray-300">
            Cancel
          </button>
        </div>
      ) : (
        <div className="text-xs text-gray-500 font-mono">
          Rollout: <span className="text-gray-300">{flag.rolloutPercentage ?? 0}%</span>
        </div>
      )}

      <div className="flex gap-2 pt-1">
        <button onClick={toggle} disabled={busy}
          className="text-xs font-mono px-3 py-1.5 rounded-lg border border-cyan-500/30 text-cyan-400 hover:bg-cyan-500/10 disabled:opacity-40 transition-colors">
          {flag.enabled ? 'Disable' : 'Enable'}
        </button>
        <button onClick={() => setEditMode(true)} disabled={busy || editMode}
          className="text-xs font-mono px-3 py-1.5 rounded-lg border border-gray-600 text-gray-400 hover:bg-gray-800 disabled:opacity-40 transition-colors">
          Edit %
        </button>
        <button onClick={deleteFlag} disabled={busy}
          className="text-xs font-mono px-3 py-1.5 rounded-lg border border-red-500/30 text-red-400 hover:bg-red-500/10 disabled:opacity-40 transition-colors ml-auto">
          Delete
        </button>
      </div>
    </div>
  )
}

export default FlagList
