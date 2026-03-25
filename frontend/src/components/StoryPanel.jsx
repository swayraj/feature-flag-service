function StoryPanel({ history }) {
  if (history.length === 0) {
    return (
      <div className="mt-10 border border-gray-800 rounded-xl p-6 bg-gray-900/50">
        <h2 className="text-sm font-bold font-mono text-gray-400 uppercase tracking-widest mb-4">
          The Story
        </h2>
        <p className="text-xs font-mono text-gray-600">
          Use the Evaluation Simulator above to start building the story.
        </p>
      </div>
    )
  }

  const total = history.length
  const hits = history.filter(e => e.cacheHit).length
  const misses = total - hits
  const hitRatio = Math.round((hits / total) * 100)

  const flagCounts = {}
  history.forEach(e => {
    flagCounts[e.flagName] = (flagCounts[e.flagName] ?? 0) + 1
  })
  const topFlags = Object.entries(flagCounts).sort((a, b) => b[1] - a[1])

  return (
    <div className="mt-10 border border-gray-800 rounded-xl p-6 bg-gray-900/50">
      <h2 className="text-sm font-bold font-mono text-gray-400 uppercase tracking-widest mb-6">
        The Story
      </h2>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <StatCard label="Total Evaluations" value={total} />
        <StatCard label="Redis Cache Hits" value={`${hits} / ${total}`} sub={`${hitRatio}% hit rate`} color="text-green-400" />
        <StatCard label="DB Fetches" value={misses} sub="cache misses" color="text-yellow-400" />
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
        <div>
          <p className="text-xs font-mono text-gray-600 uppercase tracking-widest mb-3">Evaluations per flag</p>
          <div className="flex flex-col gap-2">
            {topFlags.map(([name, count]) => (
              <div key={name} className="flex items-center gap-3">
                <span className="font-mono text-xs text-gray-300 w-36 truncate">{name}</span>
                <div className="flex-1 bg-gray-800 rounded-full h-1.5">
                  <div
                    className="bg-cyan-500 h-1.5 rounded-full transition-all"
                    style={{ width: `${(count / total) * 100}%` }}
                  />
                </div>
                <span className="font-mono text-xs text-gray-500 w-6 text-right">{count}</span>
              </div>
            ))}
          </div>
        </div>

        <div>
          <p className="text-xs font-mono text-gray-600 uppercase tracking-widest mb-3">Recent decisions</p>
          <div className="flex flex-col gap-1.5">
            {history.slice(0, 8).map((e, i) => (
              <div key={i} className="flex items-center gap-2 font-mono text-xs">
                <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${e.enabled ? 'bg-cyan-400' : 'bg-gray-600'}`} />
                <span className="text-gray-500 truncate w-28">{e.flagName}</span>
                <span className="text-gray-600 truncate w-20">{e.userId}</span>
                <span className={`ml-auto shrink-0 ${e.cacheHit ? 'text-green-500' : 'text-yellow-500'}`}>
                  {e.cacheHit ? 'hit' : 'miss'} {e.latency}ms
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

function StatCard({ label, value, sub, color = 'text-cyan-400' }) {
  return (
    <div className="bg-gray-800/60 border border-gray-700 rounded-lg p-4">
      <p className="text-xs font-mono text-gray-500 mb-1">{label}</p>
      <p className={`text-2xl font-black font-mono ${color}`}>{value}</p>
      {sub && <p className="text-xs font-mono text-gray-600 mt-1">{sub}</p>}
    </div>
  )
}

export default StoryPanel
