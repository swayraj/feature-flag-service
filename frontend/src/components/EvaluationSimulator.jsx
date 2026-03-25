import { useState } from 'react'

const API_HEADERS = {
  'Content-Type': 'application/json',
  'X-API-Key': 'test-key-123',
}

function EvaluationSimulator({ flags, onEvaluated }) {
  const [userId, setUserId] = useState('')
  const [selectedFlag, setSelectedFlag] = useState('')
  const [result, setResult] = useState(null)
  const [busy, setBusy] = useState(false)
  const [latency, setLatency] = useState(null)
  const [error, setError] = useState(null)

  function evaluate(e) {
    e.preventDefault()
    if (!userId.trim() || !selectedFlag) return
    setBusy(true)
    setResult(null)
    setError(null)

    const start = performance.now()

    fetch('/api/evaluate', {
      method: 'POST',
      headers: API_HEADERS,
      body: JSON.stringify({ flagName: selectedFlag, userId: userId.trim() }),
    })
      .then(res => {
        const ms = Math.round(performance.now() - start)
        setLatency(ms)
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json().then(data => ({ data, ms }))
      })
      .then(({ data, ms }) => {
        setResult(data)
        if (onEvaluated) onEvaluated({ ...data, latency: ms, cacheHit: ms < 8 })
      })
      .catch(err => setError(err.message))
      .finally(() => setBusy(false))
  }

  return (
    <div className="mt-10 border border-gray-800 rounded-xl p-6 bg-gray-900/50">
      <h2 className="text-sm font-bold font-mono text-gray-400 uppercase tracking-widest mb-4">
        Evaluation Simulator
      </h2>

      <form onSubmit={evaluate} className="flex flex-wrap items-end gap-4">
        <div className="flex flex-col gap-1">
          <label className="text-xs text-gray-500 font-mono">User ID</label>
          <input
            value={userId}
            onChange={e => setUserId(e.target.value)}
            placeholder="e.g. user-123"
            className="bg-gray-800 border border-gray-600 rounded px-3 py-2 text-sm font-mono text-gray-100 focus:outline-none focus:border-cyan-500 w-48"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-xs text-gray-500 font-mono">Flag</label>
          <select
            value={selectedFlag}
            onChange={e => setSelectedFlag(e.target.value)}
            className="bg-gray-800 border border-gray-600 rounded px-3 py-2 text-sm font-mono text-gray-100 focus:outline-none focus:border-cyan-500 w-48"
          >
            <option value="">— pick a flag —</option>
            {flags.map(f => (
              <option key={f.id} value={f.name}>{f.name}</option>
            ))}
          </select>
        </div>

        <button
          type="submit"
          disabled={busy || !userId.trim() || !selectedFlag}
          className="text-sm font-mono px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white disabled:opacity-40 transition-colors"
        >
          {busy ? 'Evaluating...' : 'Evaluate'}
        </button>
      </form>

      {error && (
        <p className="mt-4 text-red-400 font-mono text-sm">Error: {error}</p>
      )}

      {result && (
        <div className="mt-6 flex flex-col gap-3">
          <div className="flex items-center gap-4">
            <span className={`text-2xl font-black font-mono tracking-tight ${result.enabled ? 'text-cyan-400' : 'text-gray-500'}`}>
              {result.enabled ? 'ENABLED' : 'DISABLED'}
            </span>
            {latency !== null && (
              <span className="text-xs font-mono text-gray-600">{latency}ms</span>
            )}
          </div>

          <div className="text-xs font-mono text-gray-400 space-y-1">
            <p>
              <span className="text-gray-600">flag &nbsp;&nbsp;&nbsp;</span>
              <span className="text-gray-200">{result.flagName}</span>
            </p>
            <p>
              <span className="text-gray-600">user &nbsp;&nbsp;&nbsp;</span>
              <span className="text-gray-200">{result.userId}</span>
            </p>
            <p>
              <span className="text-gray-600">reason &nbsp;</span>
              <span className="text-gray-200">{result.reason}</span>
            </p>
          </div>
        </div>
      )}
    </div>
  )
}

export default EvaluationSimulator
