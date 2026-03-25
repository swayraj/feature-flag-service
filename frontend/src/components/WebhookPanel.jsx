import { useEffect, useState } from 'react'

const API_HEADERS = {
  'Content-Type': 'application/json',
  'X-API-Key': 'test-key-123',
}

function WebhookPanel() {
  const [webhooks, setWebhooks] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [name, setName] = useState('')
  const [url, setUrl] = useState('')
  const [busy, setBusy] = useState(false)
  const [expandedId, setExpandedId] = useState(null)

  function fetchWebhooks() {
    fetch('/api/webhooks', { headers: API_HEADERS })
      .then(res => res.json())
      .then(setWebhooks)
      .catch(() => {})
  }

  useEffect(() => { fetchWebhooks() }, [])

  function register(e) {
    e.preventDefault()
    if (!name.trim() || !url.trim()) return
    setBusy(true)
    fetch('/api/webhooks', {
      method: 'POST',
      headers: API_HEADERS,
      body: JSON.stringify({ name: name.trim(), url: url.trim(), events: 'ALL', active: true }),
    })
      .then(res => { if (!res.ok) throw new Error(`HTTP ${res.status}`); return res.json() })
      .then(() => { setName(''); setUrl(''); setShowForm(false); fetchWebhooks() })
      .catch(err => alert(err.message))
      .finally(() => setBusy(false))
  }

  function toggle(id) {
    fetch(`/api/webhooks/${id}/toggle`, { method: 'POST', headers: API_HEADERS })
      .then(() => fetchWebhooks())
      .catch(err => alert(err.message))
  }

  function remove(id) {
    if (!confirm('Delete this webhook?')) return
    fetch(`/api/webhooks/${id}`, { method: 'DELETE', headers: API_HEADERS })
      .then(() => { if (expandedId === id) setExpandedId(null); fetchWebhooks() })
      .catch(err => alert(err.message))
  }

  return (
    <div className="mt-10 border border-gray-800 rounded-xl p-6 bg-gray-900/50">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-sm font-bold font-mono text-gray-400 uppercase tracking-widest">
          Webhooks
        </h2>
        <button
          onClick={() => setShowForm(v => !v)}
          className="text-xs font-mono px-3 py-1.5 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white transition-colors"
        >
          {showForm ? 'Cancel' : '+ Register'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={register} className="flex flex-wrap items-end gap-4 mb-6 pb-6 border-b border-gray-800">
          <div className="flex flex-col gap-1">
            <label className="text-xs text-gray-500 font-mono">Name</label>
            <input
              autoFocus
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Slack alerts"
              className="bg-gray-800 border border-gray-600 rounded px-3 py-2 text-sm font-mono text-gray-100 focus:outline-none focus:border-cyan-500 w-48"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs text-gray-500 font-mono">URL</label>
            <input
              value={url}
              onChange={e => setUrl(e.target.value)}
              placeholder="https://..."
              className="bg-gray-800 border border-gray-600 rounded px-3 py-2 text-sm font-mono text-gray-100 focus:outline-none focus:border-cyan-500 w-72"
            />
          </div>
          <button type="submit" disabled={busy || !name.trim() || !url.trim()}
            className="text-sm font-mono px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white disabled:opacity-40 transition-colors">
            Register
          </button>
        </form>
      )}

      {webhooks.length === 0 ? (
        <p className="text-xs font-mono text-gray-600">No webhooks registered yet.</p>
      ) : (
        <div className="flex flex-col gap-3">
          {webhooks.map(wh => (
            <WebhookRow
              key={wh.id}
              webhook={wh}
              expanded={expandedId === wh.id}
              onExpand={() => setExpandedId(expandedId === wh.id ? null : wh.id)}
              onToggle={() => toggle(wh.id)}
              onDelete={() => remove(wh.id)}
            />
          ))}
        </div>
      )}
    </div>
  )
}

function WebhookRow({ webhook, expanded, onExpand, onToggle, onDelete }) {
  const [deliveries, setDeliveries] = useState([])
  const [loadingDeliveries, setLoadingDeliveries] = useState(false)

  function loadDeliveries() {
    setLoadingDeliveries(true)
    fetch(`/api/webhooks/${webhook.id}/deliveries`, { headers: API_HEADERS })
      .then(res => res.json())
      .then(setDeliveries)
      .catch(() => {})
      .finally(() => setLoadingDeliveries(false))
  }

  function handleExpand() {
    onExpand()
    if (!expanded) loadDeliveries()
  }

  return (
    <div className="border border-gray-800 rounded-lg overflow-hidden">
      <div className="flex items-center gap-3 px-4 py-3 bg-gray-900">
        <span className={`w-2 h-2 rounded-full shrink-0 ${webhook.active ? 'bg-cyan-400' : 'bg-gray-600'}`} />
        <div className="flex-1 min-w-0">
          <p className="font-mono text-sm text-gray-200 truncate">{webhook.name}</p>
          <p className="font-mono text-xs text-gray-600 truncate">{webhook.url}</p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <button onClick={onToggle}
            className="text-xs font-mono px-2 py-1 rounded border border-cyan-500/30 text-cyan-400 hover:bg-cyan-500/10 transition-colors">
            {webhook.active ? 'Disable' : 'Enable'}
          </button>
          <button onClick={handleExpand}
            className="text-xs font-mono px-2 py-1 rounded border border-gray-600 text-gray-400 hover:bg-gray-800 transition-colors">
            {expanded ? 'Hide log' : 'Delivery log'}
          </button>
          <button onClick={onDelete}
            className="text-xs font-mono px-2 py-1 rounded border border-red-500/30 text-red-400 hover:bg-red-500/10 transition-colors">
            Delete
          </button>
        </div>
      </div>

      {expanded && (
        <div className="border-t border-gray-800 px-4 py-3 bg-gray-950">
          {loadingDeliveries ? (
            <p className="text-xs font-mono text-gray-600">Loading deliveries...</p>
          ) : deliveries.length === 0 ? (
            <p className="text-xs font-mono text-gray-600">No deliveries yet — toggle a flag to trigger one.</p>
          ) : (
            <div className="flex flex-col gap-1.5">
              {deliveries.slice(0, 10).map(d => (
                <div key={d.id} className="flex items-center gap-3 font-mono text-xs">
                  <span className={`shrink-0 px-1.5 py-0.5 rounded text-[10px] font-bold ${
                    d.status === 'SUCCESS' ? 'bg-green-500/10 text-green-400 border border-green-500/20' : 'bg-red-500/10 text-red-400 border border-red-500/20'
                  }`}>
                    {d.status}
                  </span>
                  <span className="text-gray-500">{d.eventType}</span>
                  {d.responseCode && <span className="text-gray-600">HTTP {d.responseCode}</span>}
                  <span className="text-gray-700 ml-auto">{d.attemptCount} attempt{d.attemptCount !== 1 ? 's' : ''}</span>
                  <span className="text-gray-700">{new Date(d.deliveredAt).toLocaleTimeString()}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default WebhookPanel
