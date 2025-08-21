export interface User {
  id: string;
  email: string;
  merchantId: string;
  merchantName: string;
  role: 'ADMIN' | 'OPERATOR' | 'VIEWER';
  apiKey?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  user?: User;
  token?: string;
  apiKey?: string;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  apiKey: string | null;
  loading: boolean;
  error: string | null;
}