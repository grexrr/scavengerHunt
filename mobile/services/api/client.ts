import { API_BASE_URL } from '../../utils/constants';

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl;
  }

  async post<T>(endpoint: string, body: any): Promise<T> {
    try {
      const response = await fetch(`${this.baseUrl}${endpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
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
      if (error instanceof TypeError && error.message.includes('fetch')) {
        console.error('[ApiClient] Network error:', error);
        throw new Error(
          `Network request failed: Cannot connect to ${this.baseUrl}. Please check if the backend server is running.`
        );
      }
      throw error;
    }
  }

  async get<T>(endpoint: string): Promise<T> {
    try {
      const response = await fetch(`${this.baseUrl}${endpoint}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`API Error: ${response.status} - ${errorText}`);
      }

      return response.json();
    } catch (error) {
      // 捕获网络错误
      if (error instanceof TypeError && error.message.includes('fetch')) {
        console.error('[ApiClient] Network error:', error);
        throw new Error(
          `Network request failed: Cannot connect to ${this.baseUrl}. Please check if the backend server is running.`
        );
      }
      throw error;
    }
  }
}

export const apiClient = new ApiClient();
