import { useState } from 'react'
import FlagList from './components/FlagList'
import EvaluationSimulator from './components/EvaluationSimulator'

const API_HEADERS = {
  'Content-Type': 'application/json',
  'X-API-Key': 'test-key-123',
}

function App() {
  const [showForm, setShowForm] = useState(false)
  const [flagName, setFlagName] = useState('')
  const [rollout, setRollout] = useState(0)
  const [busy, setBusy] = useState(false)
  const [refreshKey, setRefreshKey] = useState(0)
  const [flags, setFlags] = useState([])

  function createFlag(e) {
    e.preventDefault()
    if (!flagName.trim()) return
    setBusy(true)
    fetch('/api/flags', {
      method: 'POST',
      headers: API_HEADERS,
      body: JSON.stringify({ name: flagName.trim(), enabled: false, rolloutPercentage: Number(rollout) }),
    })
      .then(res => { if (!res.ok) throw new Error(`HTTP ${res.status}`); return res.json() })
      .then(() => {
        setFlagName('')
        setRollout(0)
        setShowForm(false)
        setRefreshKey(k => k + 1)
      })
      .catch(err => {
        if (err.message.startsWith('HTTP')) {
          alert('Create failed — flag name must be 3+ chars, letters/numbers/underscores/hyphens only, and must be unique.')
        } else {
          alert(err.message)
        }
      })
      .finally(() => setBusy(false))
  }

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100">
      <header className="border-b border-gray-800 px-6 py-4 flex items-center justify-between">
        <h1 className="text-xl font-bold tracking-tight text-cyan-400 font-mono">
          {'{ CANARY }'} — Flag Dashboard
        </h1>
        <button
          onClick={() => setShowForm(v => !v)}
          className="text-xs font-mono px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white transition-colors"
        >
          {showForm ? 'Cancel' : '+ New Flag'}
        </button>
      </header>

      {showForm && (
        <form onSubmit={createFlag} className="border-b border-gray-800 px-6 py-4 flex items-end gap-4 bg-gray-900/50">
          <div className="flex flex-col gap-1">
            <label className="text-xs text-gray-500 font-mono">Flag name</label>
            <input
              autoFocus
              value={flagName}
              onChange={e => setFlagName(e.target.value)}
              placeholder="e.g. dark_mode"
              className="bg-gray-800 border border-gray-600 rounded px-3 py-2 text-sm font-mono text-gray-100 focus:outline-none focus:border-cyan-500 w-64"
            />
            <span className="text-[10px] text-gray-600 font-mono">letters, numbers, _ and - only</span>
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs text-gray-500 font-mono">Rollout %</label>
            <input
              type="number" min="0" max="100"
              value={rollout}
              onChange={e => setRollout(e.target.value)}
              className="bg-gray-800 border border-gray-600 rounded px-3 py-2 text-sm font-mono text-gray-100 focus:outline-none focus:border-cyan-500 w-24"
            />
          </div>
          <button type="submit" disabled={busy || !flagName.trim()}
            className="text-sm font-mono px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white disabled:opacity-40 transition-colors">
            Create
          </button>
        </form>
      )}

      <main className="px-6 py-8">
        <FlagList refreshKey={refreshKey} onFlagsLoaded={setFlags} />
        <EvaluationSimulator flags={flags} />
      </main>
    </div>
  )
}

export default App
