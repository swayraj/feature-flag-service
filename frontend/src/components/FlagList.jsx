import { useEffect, useState } from 'react'

const API_HEADERS = {
  'Content-Type': 'application/json',
  'X-API-Key': import.meta.env.VITE_API_KEY,
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
  if (flags.length === 0) return <p className="text-gray-500 font-mono text-sm">No flags yet — create one above.</p>

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
  const [scheduleMode, setScheduleMode] = useState(false)
  const [newRollout, setNewRollout] = useState(flag.rolloutPercentage ?? 0)
  const [busy, setBusy] = useState(false)
  const [actionError, setActionError] = useState(null)

  // one-time schedule fields
  const [schedulePercent, setSchedulePercent] = useState(100)
  const [scheduleTime, setScheduleTime] = useState('')

  // auto-rollout fields
  const [autoStep, setAutoStep] = useState(10)
  const [autoInterval, setAutoInterval] = useState(24)
  const [scheduleMsg, setScheduleMsg] = useState(null)
  const [scheduleErr, setScheduleErr] = useState(null)

  function friendlyError(res) {
    if (res.status === 403) return 'Not available in demo mode.'
    if (res.status === 401) return 'Not authorised.'
    return `Request failed (${res.status})`
  }

  function toggle() {
    setBusy(true)
    setActionError(null)
    fetch(`/api/flags/${flag.id}/toggle`, { method: 'POST', headers: API_HEADERS })
      .then(res => { if (!res.ok) throw new Error(friendlyError(res)); return res.json() })
      .then(() => onUpdate())
      .catch(err => setActionError(err.message))
      .finally(() => setBusy(false))
  }

  function deleteFlag() {
    if (!confirm(`Delete "${flag.name}"?`)) return
    setBusy(true)
    setActionError(null)
    fetch(`/api/flags/${flag.id}`, { method: 'DELETE', headers: API_HEADERS })
      .then(res => { if (!res.ok) throw new Error(friendlyError(res)) })
      .then(() => onUpdate())
      .catch(err => setActionError(err.message))
      .finally(() => setBusy(false))
  }

  function saveRollout() {
    setBusy(true)
    setActionError(null)
    fetch(`/api/flags/${flag.id}`, {
      method: 'PUT',
      headers: API_HEADERS,
      body: JSON.stringify({ name: flag.name, enabled: flag.enabled, rolloutPercentage: Number(newRollout) }),
    })
      .then(res => { if (!res.ok) throw new Error(friendlyError(res)); return res.json() })
      .then(() => { setEditMode(false); onUpdate() })
      .catch(err => setActionError(err.message))
      .finally(() => setBusy(false))
  }

  function scheduleOneTime(e) {
    e.preventDefault()
    if (!scheduleTime) return
    setScheduleMsg(null)
    setScheduleErr(null)
    setBusy(true)
    fetch('/api/schedule/rollout', {
      method: 'POST',
      headers: API_HEADERS,
      body: JSON.stringify({ flagId: flag.id, targetPercentage: Number(schedulePercent), scheduledTime: scheduleTime }),
    })
      .then(res => { if (!res.ok) throw new Error(`HTTP ${res.status}`); return res.json() })
      .then(() => setScheduleMsg(`Scheduled: rollout to ${schedulePercent}% at ${scheduleTime}`))
      .catch(err => setScheduleErr(err.message))
      .finally(() => setBusy(false))
  }

  function enableAutoRollout(e) {
    e.preventDefault()
    setScheduleMsg(null)
    setScheduleErr(null)
    setBusy(true)
    fetch('/api/schedule/auto-rollout', {
      method: 'POST',
      headers: API_HEADERS,
      body: JSON.stringify({ flagId: flag.id, step: Number(autoStep), intervalHours: Number(autoInterval) }),
    })
      .then(res => { if (!res.ok) throw new Error(`HTTP ${res.status}`); return res.json() })
      .then(() => setScheduleMsg(`Auto-rollout enabled: +${autoStep}% every ${autoInterval}h`))
      .catch(err => setScheduleErr(err.message))
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

      <div className="flex gap-2 pt-1 flex-wrap">
        <button onClick={toggle} disabled={busy}
          className="text-xs font-mono px-3 py-1.5 rounded-lg border border-cyan-500/30 text-cyan-400 hover:bg-cyan-500/10 disabled:opacity-40 transition-colors">
          {flag.enabled ? 'Disable' : 'Enable'}
        </button>
        <button onClick={() => setEditMode(true)} disabled={busy || editMode}
          className="text-xs font-mono px-3 py-1.5 rounded-lg border border-gray-600 text-gray-400 hover:bg-gray-800 disabled:opacity-40 transition-colors">
          Edit %
        </button>
        <button onClick={() => { setScheduleMode(v => !v); setScheduleMsg(null); setScheduleErr(null) }} disabled={busy}
          className={`text-xs font-mono px-3 py-1.5 rounded-lg border transition-colors disabled:opacity-40 ${scheduleMode ? 'border-yellow-500/40 text-yellow-400 bg-yellow-500/10' : 'border-gray-600 text-gray-400 hover:bg-gray-800'}`}>
          Schedule
        </button>
        <button onClick={deleteFlag} disabled={busy}
          className="text-xs font-mono px-3 py-1.5 rounded-lg border border-red-500/30 text-red-400 hover:bg-red-500/10 disabled:opacity-40 transition-colors ml-auto">
          Delete
        </button>
      </div>

      {actionError && (
        <p className="text-[11px] font-mono text-red-400">{actionError}</p>
      )}

      {scheduleMode && (
        <div className="border-t border-gray-800 pt-3 flex flex-col gap-4">
          {/* One-time rollout */}
          <div>
            <p className="text-[10px] font-mono text-gray-500 uppercase tracking-widest mb-2">One-time rollout</p>
            <form onSubmit={scheduleOneTime} className="flex flex-wrap gap-2 items-end">
              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-mono text-gray-600">Target %</label>
                <input type="number" min="0" max="100" value={schedulePercent}
                  onChange={e => setSchedulePercent(e.target.value)}
                  className="w-16 bg-gray-800 border border-gray-700 rounded px-2 py-1 text-xs font-mono text-gray-100 focus:outline-none focus:border-yellow-500" />
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-mono text-gray-600">At time</label>
                <input type="datetime-local" value={scheduleTime}
                  onChange={e => setScheduleTime(e.target.value)}
                  className="bg-gray-800 border border-gray-700 rounded px-2 py-1 text-xs font-mono text-gray-100 focus:outline-none focus:border-yellow-500" />
              </div>
              <button type="submit" disabled={busy || !scheduleTime}
                className="text-xs font-mono px-3 py-1.5 rounded bg-yellow-600 hover:bg-yellow-500 text-white disabled:opacity-40">
                Schedule
              </button>
            </form>
          </div>

          {/* Auto-rollout */}
          <div>
            <p className="text-[10px] font-mono text-gray-500 uppercase tracking-widest mb-2">Auto-rollout</p>
            <form onSubmit={enableAutoRollout} className="flex flex-wrap gap-2 items-end">
              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-mono text-gray-600">Step %</label>
                <input type="number" min="1" max="100" value={autoStep}
                  onChange={e => setAutoStep(e.target.value)}
                  className="w-16 bg-gray-800 border border-gray-700 rounded px-2 py-1 text-xs font-mono text-gray-100 focus:outline-none focus:border-yellow-500" />
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-mono text-gray-600">Every (hours)</label>
                <input type="number" min="1" value={autoInterval}
                  onChange={e => setAutoInterval(e.target.value)}
                  className="w-20 bg-gray-800 border border-gray-700 rounded px-2 py-1 text-xs font-mono text-gray-100 focus:outline-none focus:border-yellow-500" />
              </div>
              <button type="submit" disabled={busy}
                className="text-xs font-mono px-3 py-1.5 rounded bg-yellow-600 hover:bg-yellow-500 text-white disabled:opacity-40">
                Enable
              </button>
            </form>
          </div>

          {scheduleMsg && <p className="text-[11px] font-mono text-green-400">{scheduleMsg}</p>}
          {scheduleErr && <p className="text-[11px] font-mono text-red-400">Error: {scheduleErr}</p>}
        </div>
      )}
    </div>
  )
}

export default FlagList
