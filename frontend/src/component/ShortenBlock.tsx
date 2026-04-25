import { useState } from "react";
import { shortenUrl, getClickCount } from "../services/api";

export default function ShortenBlock() {
  const [url, setUrl] = useState("");
  const [shortUrl, setShortUrl] = useState("");
  const [clicks, setClicks] = useState(0);

  const handleShorten = async () => {
    if (!url) return;

    const res = await shortenUrl({ url });
    console.log("Shorten response:", res); // Debug log
    const generated = `${import.meta.env.VITE_API_BASE_URL}/${res.shortId}`;

    setShortUrl(generated);

    const count = await getClickCount(res.shortId);
    setClicks(count);
  };

  return (
    <div className="backdrop-blur-xl bg-white/5 border border-white/10 rounded-2xl p-6 shadow-[0_0_40px_rgba(59,130,246,0.15)] transition hover:scale-[1.01]">
      <h2 className="mb-3">Shorten URL</h2>

      <div className="flex gap-2">
        <input
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          className="flex-1 px-4 py-2 bg-transparent border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
        />
        <button onClick={handleShorten} className="bg-gradient-to-r from-blue-500 to-indigo-600 px-5 py-2 rounded-lg text-white font-semibold hover:scale-105 hover:shadow-lg transition-all duration-300">
          Shorten
        </button>
      </div>

      {shortUrl && (
        <div className="mt-3">
          <a href={shortUrl} target="_blank" rel="noopener noreferrer" className="text-blue-400">{shortUrl}</a>
          <p className="text-sm">Clicks: {clicks}</p>
        </div>
      )}
    </div>
  );
}