
import './App.css'
import ShortenBlock from './component/ShortenBlock';
import HistoryBlock from './component/HistoryBlock';
import ResolveBlock from './component/ResolveBlock';
export default function App() {

  return (
    <div className="relative min-h-screen overflow-hidden">

      {/* 🌌 Background */}
      <div className="absolute inset-0 bg-gradient-to-br from-[#0f172a] via-[#1e293b] to-[#020617]" />
      <div className="absolute top-[-100px] left-[-100px] w-[400px] h-[400px] bg-blue-500 blur-[120px] opacity-20 rounded-full animate-pulse" />
      <div className="absolute bottom-[-100px] right-[-100px] w-[400px] h-[400px] bg-indigo-500 blur-[120px] opacity-20 rounded-full animate-pulse" />

      {/* CONTENT */}
      <div className="relative z-10 max-w-7xl mx-auto">

        {/* Title */}
        <div className="mb-8 text-center">
          <h1 className="text-4xl font-bold text-white">
            🚀 URL Shortener
          </h1>
          <p className="text-gray-400">
            Clean links. Fast redirects. Real-time tracking.
          </p>
        </div>

        {/* 🔥 GRID LAYOUT */}
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* LEFT SIDE */}
<div className="lg:col-span-2 space-y-6">
            <ShortenBlock/>

            <ResolveBlock />

          </div>

          {/* RIGHT SIDE */}
<div className="lg:col-span-1">
            <HistoryBlock />

          </div>

        </div>

      </div>
    </div>
  );
}