import { API_BASE_URL } from "../../utils/constants";

class ApiClient {
    private baseUrl: string;

    constructor(baseUrl: string =  API_BASE_URL){
        this.baseUrl = baseUrl;
    }

    async post<T>(endpoint: string, body: any): Promise<T> {
        const response = await fetch(`${this.baseUrl}${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        });

        if (!response.ok){
            const errorText = await response.text();
            throw new Error(`API Error: ${response.status} - ${errorText}`)
        }
        
        return response.json();
    };

    async get<T>(endpoint: string): Promise<T> {
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
    }
}

export const apiClient = new ApiClient();