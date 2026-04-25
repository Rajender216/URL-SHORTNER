import type { UrlItem } from "../types/url";

import { useEffect, useState } from "react";
import { getRecentUrls } from "../services/api";



export default function HistoryBlock() {
  const [history, setHistory] = useState<UrlItem[]>([]);


  useEffect(() => {
  const load = async () => {
    try {
      const data = await getRecentUrls();
      setHistory(data);
    } catch (err) {
      console.error("Failed to fetch history", err);
    }
  };

  load();
}, []);

  return (
    <div className="backdrop-blur-xl bg-white/5 border border-white/10 rounded-2xl p-6">

      <h2 className="text-white text-lg mb-4">Recent URLs</h2>

      {history.length === 0 && (
        <p className="text-gray-400 text-sm">No history found</p>
      )}

      <div className="space-y-3 max-h-[400px] overflow-y-auto">

        {history.map((item, index) => (
          <div
            key={index}
            className="p-3 rounded-lg bg-white/5 border border-white/10"
          >
            <p className="text-gray-300 text-sm truncate">
              {item.originalUrl}
            </p>

            <a
              href={`${import.meta.env.VITE_API_BASE_URL}/${item.shortId}`}
              target="_blank"
              rel="noopener noreferrer"
              className="text-blue-400 text-sm"
            >
              {item.shortId}
            </a>

            <p className="text-xs text-gray-400">
              Clicks: {item.clickCount}
            </p>
          </div>
        ))}

      </div>
    </div>
  );
}