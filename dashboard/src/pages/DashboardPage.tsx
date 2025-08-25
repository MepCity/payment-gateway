import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  CircularProgress,
  Alert,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  CreditCard,
  AccountBalance,
  Group,
  MoneyOff,
  Add,
} from '@mui/icons-material';
import StatsCards, { StatsCard } from '../components/common/StatsCards';
import { dashboardApi } from '../services/dashboardApi';
import CreateDisputeModal, { CreateDisputeFormData } from '../components/disputes/CreateDisputeModal';
import { useAuth } from '../contexts/AuthContext';

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
  const { state: authState } = useAuth();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  const fetchDashboardStats = async () => {
    try {
      setLoading(true);
      setError(null);
      
      console.log('🔄 Fetching dashboard stats...');
      // Fetch dashboard stats from backend
      const response = await dashboardApi.getDashboardStats();
      console.log('📊 Dashboard stats response:', response.data);
      setStats(response.data);
    } catch (err: any) {
      console.error('Error fetching dashboard stats:', err);
      setError(err.response?.data?.message || 'Failed to fetch dashboard statistics');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateDispute = async (data: CreateDisputeFormData) => {
    try {
      console.log('📝 Creating dispute with data:', data);
      const result = await dashboardApi.createDispute(data);
      
      if (result.success) {
        console.log('✅ Dispute created successfully:', result.disputeId);
        console.log('🔄 Refreshing dashboard stats after dispute creation...');
        // Refresh dashboard stats after successful creation
        await fetchDashboardStats();
        console.log('✅ Dashboard stats refreshed after dispute creation');
      } else {
        throw new Error(result.message || 'Dispute oluşturulamadı.');
      }
    } catch (error: any) {
      console.error('❌ Error creating dispute:', error);
      throw error;
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
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: '1fr 1fr 1fr', lg: '1fr 1fr 1fr 1fr 1fr 1fr' }, 
          gap: 3 
        }}>
          {/* Total Payments */}
          <Card>
            <CardContent>
              <Typography variant="h6" color="text.secondary">
                Total Payments
              </Typography>
              <Typography variant="h4" fontWeight="bold" color="primary.main">
                {stats.totalPayments.toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                ${stats.totalAmount.toLocaleString()} total volume
              </Typography>
            </CardContent>
          </Card>

          {/* Success Rate */}
          <Card>
            <CardContent>
              <Typography variant="h6" color="text.secondary">
                Success Rate
              </Typography>
              <Typography variant="h4" fontWeight="bold" color="success.main">
                {stats.successRate.toFixed(1)}%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {stats.pendingPayments} pending payments
              </Typography>
            </CardContent>
          </Card>

          {/* Total Refunds */}
          <Card>
            <CardContent>
              <Typography variant="h6" color="text.secondary">
                Total Refunds
              </Typography>
              <Typography variant="h4" fontWeight="bold" color="warning.main">
                {stats.totalRefunds.toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                ${stats.refundAmount.toLocaleString()} refunded
              </Typography>
            </CardContent>
          </Card>

          {/* Active Customers */}
          <Card>
            <CardContent>
              <Typography variant="h6" color="text.secondary">
                Active Customers
              </Typography>
              <Typography variant="h4" fontWeight="bold" color="info.main">
                {stats.totalCustomers.toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Registered customers
              </Typography>
            </CardContent>
          </Card>

          {/* Total Disputes with Create Button */}
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h6" color="text.secondary">
                    Total Disputes
                  </Typography>
                  <Typography variant="h4" fontWeight="bold" color="error.main">
                    {stats.totalDisputes.toLocaleString()}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {stats.pendingDisputes} pending review
                  </Typography>
                </Box>
                <Tooltip title="Yeni Dispute Oluştur">
                  <IconButton
                    color="primary"
                    onClick={() => setCreateModalOpen(true)}
                    sx={{ 
                      bgcolor: 'primary.main', 
                      color: 'white',
                      '&:hover': { bgcolor: 'primary.dark' }
                    }}
                  >
                    <Add />
                  </IconButton>
                </Tooltip>
              </Box>
            </CardContent>
          </Card>

          {/* Dispute Rate */}
          <Card>
            <CardContent>
              <Typography variant="h6" color="text.secondary">
                Dispute Rate
              </Typography>
              <Typography variant="h4" fontWeight="bold" color="secondary.main">
                {stats.disputeRate.toFixed(2)}%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Disputes vs payments ratio
              </Typography>
            </CardContent>
          </Card>
        </Box>
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

      {/* Create Dispute Modal */}
      <CreateDisputeModal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onSubmit={handleCreateDispute}
        merchantId={authState.user?.merchantId || 'MERCH001'}
      />
    </Box>
  );
};

export default DashboardPage;
