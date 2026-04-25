import { useState } from "react";

export default function ResolveBlock() {
  const [shortId, setShortId] = useState("");
  const [url, setUrl] = useState("");

  const handleResolve = () => {
    if (!shortId) return;
    setUrl(`${import.meta.env.VITE_API_BASE_URL}/${shortId}`);
  };

  return (
    <div className="backdrop-blur-xl bg-white/5 border border-white/10 rounded-2xl p-6 shadow-[0_0_40px_rgba(59,130,246,0.2)] transition-all duration-300 hover:scale-[1.02]">
      <h2 className="mb-3">Get Original URL</h2>

      <div className="flex gap-2">
        <input
          value={shortId}
          onChange={(e) => setShortId(e.target.value)}
          className="flex-1 px-4 py-2 bg-transparent border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
        />
        <button onClick={handleResolve} className="bg-gradient-to-r from-blue-500 to-indigo-600 px-5 py-2 rounded-lg text-white font-semibold hover:scale-105 hover:shadow-lg transition-all duration-300">
          Get
        </button>
      </div>

      {url && (
        <a href={url} className="text-green-400 mt-3 block">
          {url}
        </a>
      )}
    </div>
  );
}