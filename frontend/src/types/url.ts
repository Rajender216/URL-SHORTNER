export interface ShortenRequest {
  url: string;
}

export interface ShortenResponse {
    data: Data;
    message: string;
    statusCode : number;
}

export interface Data {
    shortId: string;
}

export interface UrlItem {
  shortId: string;
  originalUrl: string;
  clickCount: number;
};