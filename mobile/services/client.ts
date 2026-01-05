import { API_BASE_URL } from '../utils/constants';

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl;

    // logs
    console.log('[ApiClient] Initialized with baseUrl:', this.baseUrl);
  }

  async post<T>(endpoint: string, body: any): Promise<T> {

    // logs
    const url = `${this.baseUrl}${endpoint}`;
    console.log('[ApiClient] POST request to:', url);

    try {
      const response = await fetch(`${this.baseUrl}${endpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true',
        },
        body: JSON.stringify(body),
      });
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`API Error: ${response.status} - ${errorText}`);
      }

      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return response.json();
      } else {
        const text = await response.text();
        return text as T;
      }
    } catch (error) {
      // 详细错误日志
      console.error('[ApiClient] POST error - URL:', url);
      console.error('[ApiClient] POST error - Error type:', error?.constructor?.name);
      console.error('[ApiClient] POST error - Error message:', error instanceof Error ? error.message : String(error));
      console.error('[ApiClient] POST error - Full error:', error);
      if (error instanceof Error) {
        console.error('[ApiClient] POST error - Stack:', error.stack);
        console.error('[ApiClient] POST error - Name:', error.name);
      }
      // 尝试获取更多错误信息
      if (error && typeof error === 'object') {
        console.error('[ApiClient] POST error - Error keys:', Object.keys(error));
        for (const key in error) {
          console.error(`[ApiClient] POST error - ${key}:`, (error as any)[key]);
        }
      }

      if (error instanceof TypeError && error.message.includes('fetch')) {
        throw new Error(
          `Network request failed: Cannot connect to ${this.baseUrl}. Please check if the backend server is running.`
        );
      }
      throw error;
    }
  }

  async get<T>(endpoint: string): Promise<T> {

    // logs
    const url = `${this.baseUrl}${endpoint}`;
    console.log('[ApiClient] GET request to:', url);

    try {
      const response = await fetch(`${this.baseUrl}${endpoint}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true',
        },
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`API Error: ${response.status} - ${errorText}`);
      }

      return response.json();
    } catch (error) {
      // 详细错误日志
      console.error('[ApiClient] GET error - URL:', url);
      console.error('[ApiClient] GET error - Error type:', error?.constructor?.name);
      console.error('[ApiClient] GET error - Error message:', error instanceof Error ? error.message : String(error));
      console.error('[ApiClient] GET error - Full error:', error);
      if (error instanceof Error) {
        console.error('[ApiClient] GET error - Stack:', error.stack);
        console.error('[ApiClient] GET error - Name:', error.name);
      }
      // 尝试获取更多错误信息
      if (error && typeof error === 'object') {
        console.error('[ApiClient] GET error - Error keys:', Object.keys(error));
        for (const key in error) {
          console.error(`[ApiClient] GET error - ${key}:`, (error as any)[key]);
        }
      }

      // 捕获网络错误
      if (error instanceof TypeError && error.message.includes('fetch')) {
        throw new Error(
          `Network request failed: Cannot connect to ${this.baseUrl}. Please check if the backend server is running.`
        );
      }
      throw error;
    }
  }
}

export const apiClient = new ApiClient();
