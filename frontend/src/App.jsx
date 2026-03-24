  import FlagList from './components/FlagList'

  function App() {
    return (
      <div className="min-h-screen bg-gray-950 text-gray-100">
        <header className="border-b border-gray-800 px-6 py-4">
          <h1 className="text-xl font-bold tracking-tight text-cyan-400 font-mono">
            {'{ CANARY }'} — Flag Dashboard
          </h1>
        </header>
        <main className="px-6 py-8">
          <FlagList />
        </main>
      </div>
    )
  }

  export default App