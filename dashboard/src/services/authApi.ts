import axios from 'axios';
import { LoginRequest, LoginResponse } from '../types/auth';

const API_BASE_URL = 'http://localhost:8080';

const authApiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const authAPI = {
  // Backend authentication endpoint
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    try {
      // Real backend authentication endpoint
      const response = await authApiClient.post('/v1/auth/login', credentials);
      return response.data;

    } catch (error: any) {
      console.error('Login error:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Login failed'
      };
    }
  },

  // Logout (if needed for backend cleanup)
  logout: async (): Promise<void> => {
    // TODO: Implement logout endpoint if needed
    // await authApiClient.post('/v1/auth/logout');
  },

  // Get current user profile
  getProfile: async (token: string) => {
    // TODO: Implement get profile endpoint
    // return await authApiClient.get('/v1/auth/profile', {
    //   headers: { Authorization: `Bearer ${token}` }
    // });
  }
};