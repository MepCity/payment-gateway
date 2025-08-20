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
  // Real backend authentication
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    try {
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
    try {
      const token = localStorage.getItem('auth_token');
      if (token) {
        await authApiClient.post('/v1/auth/logout', {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
      }
    } catch (error) {
      console.error('Logout error:', error);
    }
  },

  // Get current user profile
  getProfile: async (token: string) => {
    try {
      const response = await authApiClient.get('/v1/auth/profile', {
        headers: { Authorization: `Bearer ${token}` }
      });
      return response.data;
    } catch (error) {
      console.error('Get profile error:', error);
      throw error;
    }
  }
};