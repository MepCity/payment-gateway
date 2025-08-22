import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  CreditCard,
  AccountBalance,
  Group,
  MoneyOff,
} from '@mui/icons-material';
import StatsCards, { StatsCard } from '../components/common/StatsCards';
import { dashboardApi } from '../services/dashboardApi';

interface DashboardStats {
  totalPayments: number;
  totalAmount: number;
  successRate: number;
  pendingPayments: number;
  totalRefunds: number;
  refundAmount: number;
  totalCustomers: number;
  totalDisputes: number;
  pendingDisputes: number;
  disputeRate: number;
}

const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  const fetchDashboardStats = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Fetch dashboard stats from backend
      const response = await dashboardApi.getDashboardStats();
      setStats(response.data);
    } catch (err: any) {
      console.error('Error fetching dashboard stats:', err);
      setError(err.response?.data?.message || 'Failed to fetch dashboard statistics');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '400px' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  if (!stats) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="info">No dashboard data available</Alert>
      </Box>
    );
  }

  const statsCards: StatsCard[] = [
    {
      title: 'Total Payments',
      value: stats.totalPayments.toLocaleString(),
      subtitle: `$${stats.totalAmount.toLocaleString()} total volume`,
      color: 'primary'
    },
    {
      title: 'Success Rate',
      value: `${stats.successRate.toFixed(1)}%`,
      subtitle: `${stats.pendingPayments} pending payments`,
      color: 'success'
    },
    {
      title: 'Total Refunds',
      value: stats.totalRefunds.toLocaleString(),
      subtitle: `$${stats.refundAmount.toLocaleString()} refunded`,
      color: 'warning'
    },
    {
      title: 'Active Customers',
      value: stats.totalCustomers.toLocaleString(),
      subtitle: 'Registered customers',
      color: 'info'
    },
    {
      title: 'Total Disputes',
      value: stats.totalDisputes.toLocaleString(),
      subtitle: `${stats.pendingDisputes} pending review`,
      color: 'error'
    },
    {
      title: 'Dispute Rate',
      value: `${stats.disputeRate.toFixed(2)}%`,
      subtitle: 'Disputes vs payments ratio',
      color: 'secondary'
    }
  ];

  return (
    <Box>
      {/* Page Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          Dashboard Overview
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Real-time statistics and insights for your payment gateway
        </Typography>
      </Box>

      {/* Statistics Cards */}
      <Box sx={{ mb: 4 }}>
        <StatsCards cards={statsCards} />
      </Box>

      {/* Recent Activity Grid */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 3 }}>
        {/* Payment Activity */}
        <Card>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <CreditCard sx={{ mr: 1, color: 'primary.main' }} />
              <Typography variant="h6">Recent Payments</Typography>
            </Box>
            <Typography variant="body2" color="text.secondary">
              Latest payment transactions and their status
            </Typography>
            <Box sx={{ 
              mt: 2, 
              p: 2, 
              bgcolor: 'action.hover', 
              borderRadius: 1,
              border: '1px solid',
              borderColor: 'divider'
            }}>
              <Typography variant="body2" color="text.primary">
                • {stats.totalPayments} total payments processed
              </Typography>
              <Typography variant="body2" color="text.primary">
                • ${stats.totalAmount.toLocaleString()} total volume
              </Typography>
              <Typography variant="body2" color="text.primary">
                • {stats.pendingPayments} payments pending
              </Typography>
            </Box>
          </CardContent>
        </Card>

        {/* Refund Activity */}
        <Card>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <MoneyOff sx={{ mr: 1, color: 'warning.main' }} />
              <Typography variant="h6">Refund Activity</Typography>
            </Box>
            <Typography variant="body2" color="text.secondary">
              Refund requests and processing status
            </Typography>
            <Box sx={{ 
              mt: 2, 
              p: 2, 
              bgcolor: 'action.hover', 
              borderRadius: 1,
              border: '1px solid',
              borderColor: 'divider'
            }}>
              <Typography variant="body2" color="text.primary">
                • {stats.totalRefunds} total refunds issued
              </Typography>
              <Typography variant="body2" color="text.primary">
                • ${stats.refundAmount.toLocaleString()} refunded amount
              </Typography>
              <Typography variant="body2" color="text.primary">
                • Processing time: avg 2-3 business days
              </Typography>
            </Box>
          </CardContent>
        </Card>

        {/* Customer Insights */}
        <Card>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <Group sx={{ mr: 1, color: 'info.main' }} />
              <Typography variant="h6">Customer Insights</Typography>
            </Box>
            <Typography variant="body2" color="text.secondary">
              Customer base and engagement metrics
            </Typography>
            <Box sx={{ 
              mt: 2, 
              p: 2, 
              bgcolor: 'action.hover', 
              borderRadius: 1,
              border: '1px solid',
              borderColor: 'divider'
            }}>
              <Typography variant="body2" color="text.primary">
                • {stats.totalCustomers} active customers
              </Typography>
              <Typography variant="body2" color="text.primary">
                • Growing customer base
              </Typography>
              <Typography variant="body2" color="text.primary">
                • High customer satisfaction rate
              </Typography>
            </Box>
          </CardContent>
        </Card>

        {/* Dispute Management */}
        <Card>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <AccountBalance sx={{ mr: 1, color: 'error.main' }} />
              <Typography variant="h6">Dispute Management</Typography>
            </Box>
            <Typography variant="body2" color="text.secondary">
              Dispute tracking and resolution status
            </Typography>
            <Box sx={{ 
              mt: 2, 
              p: 2, 
              bgcolor: 'action.hover', 
              borderRadius: 1,
              border: '1px solid',
              borderColor: 'divider'
            }}>
              <Typography variant="body2" color="text.primary">
                • {stats.totalDisputes} total disputes received
              </Typography>
              <Typography variant="body2" color="text.primary">
                • {stats.pendingDisputes} disputes pending review
              </Typography>
              <Typography variant="body2" color="text.primary">
                • {stats.disputeRate.toFixed(2)}% dispute rate
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
};

export default DashboardPage;
