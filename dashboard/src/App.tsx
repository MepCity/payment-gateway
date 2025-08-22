import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { AuthProvider } from './contexts/AuthContext';
import { ThemeProvider as CustomThemeProvider, useTheme } from './contexts/ThemeContext';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LoginPage from './components/auth/LoginPage';
import DashboardLayout from './components/layout/DashboardLayout';
import DashboardPage from './pages/DashboardPage';
import ProcessPaymentPage from './pages/ProcessPaymentPage';
import PaymentsPage from './pages/PaymentsPage';
import PaymentDetailPage from './pages/PaymentDetailPage';
import RefundsPage from './pages/RefundsPage';
import RefundDetailPage from './pages/RefundDetailPage';
import CustomersPage from './pages/CustomersPage';
import CustomerDetailPage from './pages/CustomerDetailPage';
import DisputesPage from './pages/DisputesPage';
import DisputeDetailPage from './pages/DisputeDetailPage';

// Create dynamic theme that responds to custom theme context
const createAppTheme = (mode: 'light' | 'dark') => createTheme({
  palette: {
    mode,
    primary: {
      main: mode === 'light' ? '#1976d2' : '#60a5fa',
      light: mode === 'light' ? '#42a5f5' : '#93c5fd',
      dark: mode === 'light' ? '#1565c0' : '#3b82f6',
    },
    secondary: {
      main: mode === 'light' ? '#dc004e' : '#f87171',
    },
    background: {
      default: mode === 'light' ? '#f5f5f5' : '#0f172a',
      paper: mode === 'light' ? '#ffffff' : '#1e293b',
    },
    text: {
      primary: mode === 'light' ? '#212121' : '#f1f5f9',
      secondary: mode === 'light' ? '#757575' : '#94a3b8',
    },
    divider: mode === 'light' ? '#e2e8f0' : '#334155',
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 600,
    },
    h5: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 600,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 8,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        },
      },
    },
  },
});

function App() {
  return (
    <CustomThemeProvider>
      <AppContent />
    </CustomThemeProvider>
  );
}

function AppContent() {
  const { theme: customTheme } = useTheme();
  const muiTheme = createAppTheme(customTheme);
  
  return (
    <ThemeProvider theme={muiTheme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />
          
          {/* Protected Routes */}
          <Route path="/dashboard/*" element={
            <ProtectedRoute>
              <DashboardLayout />
            </ProtectedRoute>
          }>
            {/* Default route - Dashboard Overview */}
            <Route index element={<DashboardPage />} />
            
            {/* Dashboard Overview */}
            <Route path="overview" element={<DashboardPage />} />
            
            {/* Process Payment */}
            <Route path="process-payment" element={<ProcessPaymentPage />} />
            
            {/* Payments */}
            <Route path="payments" element={<PaymentsPage />} />
            <Route path="payments/:paymentId" element={<PaymentDetailPage />} />
            
            {/* Refunds */}
            <Route path="refunds" element={<RefundsPage />} />
            <Route path="refunds/:refundId" element={<RefundDetailPage />} />
            
            {/* Customers */}
            <Route path="customers" element={<CustomersPage />} />
            <Route path="customers/:customerId" element={<CustomerDetailPage />} />
            
            {/* Disputes - Our feature */}
            <Route path="disputes" element={<DisputesPage />} />
            <Route path="disputes/:disputeId" element={<DisputeDetailPage />} />
            
            {/* Other routes - to be implemented */}
            <Route path="analytics" element={<div>Analytics Page - Coming Soon</div>} />
            <Route path="webhooks" element={<div>Webhooks Page - Coming Soon</div>} />
            <Route path="settings" element={<div>Settings Page - Coming Soon</div>} />
          </Route>

          {/* Default Redirect */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
    </ThemeProvider>
  );
}

export default App;