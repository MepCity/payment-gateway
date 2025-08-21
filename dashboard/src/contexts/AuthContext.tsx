import React, { createContext, useContext, useReducer, useEffect } from 'react';
import { User, AuthState, LoginRequest, LoginResponse } from '../types/auth';
import { authAPI } from '../services/authApi';

interface AuthContextType {
  state: AuthState;
  login: (credentials: LoginRequest) => Promise<boolean>;
  logout: () => void;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

type AuthAction = 
  | { type: 'LOGIN_START' }
  | { type: 'LOGIN_SUCCESS'; payload: { user: User; token: string; apiKey: string } }
  | { type: 'LOGIN_FAILURE'; payload: string }
  | { type: 'LOGOUT' }
  | { type: 'CLEAR_ERROR' }
  | { type: 'RESTORE_SESSION'; payload: { user: User; token: string; apiKey: string } };

const initialState: AuthState = {
  isAuthenticated: false,
  user: null,
  token: null,
  apiKey: null,
  loading: true, // Start with loading true to prevent flash during session restore
  error: null,
};

function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'LOGIN_START':
      return { ...state, loading: true, error: null };
    
    case 'LOGIN_SUCCESS':
      return {
        ...state,
        isAuthenticated: true,
        user: action.payload.user,
        token: action.payload.token,
        apiKey: action.payload.apiKey,
        loading: false,
        error: null,
      };
    
    case 'LOGIN_FAILURE':
      return {
        ...state,
        isAuthenticated: false,
        user: null,
        token: null,
        apiKey: null,
        loading: false,
        error: action.payload,
      };
    
    case 'LOGOUT':
      return {
        ...initialState,
        loading: false, // Set loading to false when logging out
      };
    
    case 'CLEAR_ERROR':
      return { ...state, error: null };
    
    case 'RESTORE_SESSION':
      return {
        ...state,
        isAuthenticated: true,
        user: action.payload.user,
        token: action.payload.token,
        apiKey: action.payload.apiKey,
        loading: false, // Set loading to false after session restore
      };
    
    default:
      return state;
  }
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Session restore on app load
  useEffect(() => {
    const restoreSession = () => {
      const token = localStorage.getItem('auth_token');
      const userStr = localStorage.getItem('auth_user');
      const apiKey = localStorage.getItem('auth_api_key');

      if (token && userStr && apiKey) {
        try {
          const user = JSON.parse(userStr);
          dispatch({
            type: 'RESTORE_SESSION',
            payload: { user, token, apiKey }
          });
        } catch (error) {
          // Invalid stored data, clear it
          localStorage.removeItem('auth_token');
          localStorage.removeItem('auth_user');
          localStorage.removeItem('auth_api_key');
          // Set loading to false if session restore fails
          dispatch({ type: 'LOGOUT' });
        }
      } else {
        // No session to restore, set loading to false
        dispatch({ type: 'LOGOUT' });
      }
    };

    restoreSession();
  }, []);

  const login = async (credentials: LoginRequest): Promise<boolean> => {
    dispatch({ type: 'LOGIN_START' });

    try {
      const response: LoginResponse = await authAPI.login(credentials);
      
      if (response.success && response.user && response.token && response.apiKey) {
        // Store in localStorage
        localStorage.setItem('auth_token', response.token);
        localStorage.setItem('auth_user', JSON.stringify(response.user));
        localStorage.setItem('auth_api_key', response.apiKey);

        dispatch({
          type: 'LOGIN_SUCCESS',
          payload: {
            user: response.user,
            token: response.token,
            apiKey: response.apiKey
          }
        });

        return true;
      } else {
        dispatch({
          type: 'LOGIN_FAILURE',
          payload: response.message || 'Login failed'
        });
        return false;
      }
    } catch (error: any) {
      dispatch({
        type: 'LOGIN_FAILURE',
        payload: error.message || 'Network error occurred'
      });
      return false;
    }
  };

  const logout = () => {
    // Clear localStorage
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    localStorage.removeItem('auth_api_key');

    dispatch({ type: 'LOGOUT' });
  };

  const clearError = () => {
    dispatch({ type: 'CLEAR_ERROR' });
  };

  const value: AuthContextType = {
    state,
    login,
    logout,
    clearError,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};