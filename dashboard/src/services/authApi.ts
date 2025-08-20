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
  // Mock login - replace with real API call
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    try {
      // TODO: Replace with real backend authentication endpoint
      // const response = await authApiClient.post('/v1/auth/login', credentials);
      // return response.data;
      
      // Mock implementation for now  
      if (credentials.email === 'merchant@test.com' && credentials.password === 'password') {
        const apiKey = 'pk_merch001_live_abc123'; // Use the valid API key
        
        return {
          success: true,
          message: 'Login successful',
          user: {
            id: '1',
            email: credentials.email,
            merchantId: 'TEST_MERCHANT',
            merchantName: 'Test Merchant',
            role: 'ADMIN',
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
          token: 'mock-jwt-token-' + Date.now(),
          apiKey: apiKey
        };
      } else {
        return {
          success: false,
          message: 'Invalid email or password'
        };
      }
    } catch (error: any) {
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
