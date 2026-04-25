import axios from "axios";
import type { Data, ShortenRequest } from "../types/url";

const API = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});



export const shortenUrl = async (data: ShortenRequest): Promise<Data> => {
   console.log("Shortening URL:", import.meta.env.VITE_API_BASE_URL); // Debug log
  const res = await API.post("/shorten", data);
  return res.data.data;
};

export const getOriginalUrl = async (shortId: string): Promise<string> => {
  // backend redirects → so we just build URL
  return `${import.meta.env.VITE_API_BASE_URL}/resolve/${shortId}`;
};

export const getClickCount = async (shortId: string): Promise<number> => {
  const res = await API.get(`/stats/${shortId}`);
  return res.data.clicks;
};

export const getRecentUrls = async () => {
  const res = await API.get("/recent");
  return res.data; // array of urls
};