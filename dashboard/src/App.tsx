import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LoginPage from './components/auth/LoginPage';
import DashboardLayout from './components/layout/DashboardLayout';
import ProcessPaymentPage from './pages/ProcessPaymentPage';
import PaymentsPage from './pages/PaymentsPage';
import PaymentDetailPage from './pages/PaymentDetailPage';
import RefundsPage from './pages/RefundsPage';
import ProcessRefundPage from './pages/ProcessRefundPage';
import RefundDetailPage from './pages/RefundDetailPage';
import WebhooksPage from './pages/WebhooksPage';

// Create theme similar to Hyperswitch
const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
      light: '#42a5f5',
      dark: '#1565c0',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#f5f5f5',
      paper: '#ffffff',
    },
    text: {
      primary: '#212121',
      secondary: '#757575',
    },
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
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<LoginPage />} />
            
            {/* Protected Routes */}
            <Route path="/dashboard" element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }>
              {/* Default route - Process Payment */}
              <Route index element={<ProcessPaymentPage />} />
              
              {/* Process Payment */}
              <Route path="process-payment" element={<ProcessPaymentPage />} />
              
              {/* Payments */}
              <Route path="payments" element={<PaymentsPage />} />
              <Route path="payments/:paymentId" element={<PaymentDetailPage />} />
              
              {/* Refunds */}
              <Route path="refunds" element={<RefundsPage />} />
              <Route path="refunds/process" element={<ProcessRefundPage />} />
              <Route path="refunds/:refundId" element={<RefundDetailPage />} />
              
              {/* Other routes - to be implemented */}
              <Route path="disputes" element={<div>Disputes Page - Coming Soon</div>} />
              <Route path="customers" element={<div>Customers Page - Coming Soon</div>} />
              <Route path="analytics" element={<div>Analytics Page - Coming Soon</div>} />
              <Route path="webhooks" element={<WebhooksPage />} />
              <Route path="settings" element={<div>Settings Page - Coming Soon</div>} />
            </Route>

            {/* Default Redirect */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            
            {/* 404 Catch All */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
