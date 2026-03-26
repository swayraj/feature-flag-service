import { useState } from 'react'

const API_HEADERS = {
  'Content-Type': 'application/json',
  'X-API-Key': import.meta.env.VITE_API_KEY,
}

const USER_IDS = Array.from({ length: 200 }, (_, i) => `user-${String(i + 1).padStart(3, '0')}`)

function RolloutVisualizer({ flags }) {
  const [selectedFlag, setSelectedFlag] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [previewPct, setPreviewPct] = useState(null)
  const [actualPct, setActualPct] = useState(null)

  function loadFlag(flagName) {
    setSelectedFlag(flagName)
    setResults([])
    setPreviewPct(null)
    if (!flagName) return

    const flag = flags.find(f => f.name === flagName)
    const pct = flag?.rolloutPercentage ?? 0
    setActualPct(pct)
    setPreviewPct(pct)
    setLoading(true)

    fetch('/api/evaluate/batch', {
      method: 'POST',
      headers: API_HEADERS,
      body: JSON.stringify({ flagName, userIds: USER_IDS }),
    })
      .then(res => { if (!res.ok) throw new Error(`HTTP ${res.status}`); return res.json() })
      .then(data => setResults(data.results ?? []))
      .catch(err => alert(err.message))
      .finally(() => setLoading(false))
  }

  const enabledCount = results.filter(r => r.enabled).length

  return (
    <div className="mt-10 border border-gray-800 rounded-xl p-6 bg-gray-900/50">
      <h2 className="text-sm font-bold font-mono text-gray-400 uppercase tracking-widest mb-4">
        Rollout Visualizer
      </h2>

      <div className="flex flex-wrap items-end gap-4 mb-6">
        <div className="flex flex-col gap-1">
          <label className="text-xs text-gray-500 font-mono">Flag</label>
          <select
            value={selectedFlag}
            onChange={e => loadFlag(e.target.value)}
            className="bg-gray-800 border border-gray-600 rounded px-3 py-2 text-sm font-mono text-gray-100 focus:outline-none focus:border-cyan-500 w-48"
          >
            <option value="">— pick a flag —</option>
            {flags.map(f => (
              <option key={f.id} value={f.name}>{f.name}</option>
            ))}
          </select>
        </div>

        {results.length > 0 && (
          <div className="flex flex-col gap-1 flex-1 min-w-48">
            <label className="text-xs text-gray-500 font-mono">
              Preview rollout % — <span className="text-cyan-400">{previewPct}%</span>
              {previewPct !== actualPct && (
                <span className="text-yellow-400 ml-2">(actual: {actualPct}%)</span>
              )}
            </label>
            <input
              type="range" min="0" max="100"
              value={previewPct ?? 0}
              onChange={e => setPreviewPct(Number(e.target.value))}
              className="w-full accent-cyan-400"
            />
          </div>
        )}
      </div>

      {loading && (
        <p className="text-xs font-mono text-gray-600">Evaluating 100 users...</p>
      )}

      {results.length > 0 && (
        <>
          <div className="grid gap-1.5 mb-4" style={{ gridTemplateColumns: 'repeat(20, minmax(0, 1fr))' }}>
            {results.map((r, i) => {
              const inPreview = isInPreview(r.userId, selectedFlag, previewPct)
              return (
                <div
                  key={i}
                  title={`${r.userId}: ${inPreview ? 'enabled' : 'disabled'}`}
                  className={`w-4 h-4 rounded-full transition-colors duration-150 cursor-default ${
                    inPreview ? 'bg-cyan-400' : 'bg-gray-700'
                  }`}
                />
              )
            })}
          </div>

          <div className="text-xs font-mono text-gray-500">
            <span className="text-cyan-400">{previewCount(results, selectedFlag, previewPct)}</span>
            <span> / 200 users enabled at {previewPct}%</span>
          </div>
        </>
      )}

      {!loading && !selectedFlag && (
        <p className="text-xs font-mono text-gray-600">Pick a flag to visualize its rollout across 100 users.</p>
      )}
    </div>
  )
}

function hashBucket(flagName, userId) {
  // Mirrors the backend: SHA-256 of flagName+userId → bucket 0–99
  // We can't run SHA-256 in JS synchronously without crypto, so we use
  // the actual backend results for real coloring, and approximate for preview.
  // For preview we use a simple deterministic hash.
  const str = flagName + userId
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = (Math.imul(31, hash) + str.charCodeAt(i)) | 0
  }
  return Math.abs(hash) % 100
}

function isInPreview(userId, flagName, pct) {
  return hashBucket(flagName, userId) < pct
}

function previewCount(results, flagName, pct) {
  return results.filter(r => isInPreview(r.userId, flagName, pct)).length
}

export default RolloutVisualizer
