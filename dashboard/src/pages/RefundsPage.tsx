import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Tooltip,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  Add as AddIcon,
  Refresh as RefreshIcon,
  Visibility as VisibilityIcon,
  ContentCopy as CopyIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { RefundDetail, RefundStatus } from '../types/dashboard';
import StatusChip from '../components/common/StatusChip';

const RefundsPage: React.FC = () => {
  const navigate = useNavigate();
  const [refunds, setRefunds] = useState<RefundDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Initial mock refunds for demo purposes (only shown if no refunds exist in localStorage)
  const initialMockRefunds: RefundDetail[] = [
    {
      id: 1,
      refundId: 'ref_123456789',
      paymentId: 'pay_O7H7KL7PWnc92drRvE8Z',
      merchantId: 'MERCH001',
      customerId: 'CUST001',
      amount: 15.50,
      currency: 'USD',
      status: RefundStatus.COMPLETED,
      reason: 'Customer requested refund',
      description: 'Refund for duplicate charge',
      gatewayResponse: 'Refund processed successfully',
      gatewayTransactionId: 'REF-GTW-X1Y2Z3W4',
      createdAt: new Date(Date.now() - 86400000).toISOString(), // 1 day ago
      updatedAt: new Date(Date.now() - 3600000).toISOString(), // 1 hour ago
      completedAt: new Date(Date.now() - 1800000).toISOString(), // 30 min ago
    },
    {
      id: 2,
      refundId: 'ref_987654321',
      paymentId: 'pay_ABC123DEF456',
      merchantId: 'MERCH001',
      customerId: 'CUST002',
      amount: 29.99,
      currency: 'USD',
      status: RefundStatus.PENDING,
      reason: 'Product not received',
      description: 'Refund for undelivered item',
      gatewayResponse: 'Refund request submitted',
      gatewayTransactionId: 'REF-GTW-ABC123',
      createdAt: new Date(Date.now() - 7200000).toISOString(), // 2 hours ago
      updatedAt: new Date(Date.now() - 7200000).toISOString(),
      completedAt: undefined,
    },
    {
      id: 3,
      refundId: 'ref_456789123',
      paymentId: 'pay_XYZ789ABC456',
      merchantId: 'MERCH001',
      customerId: 'CUST003',
      amount: 45.00,
      currency: 'USD',
      status: RefundStatus.FAILED,
      reason: 'Technical issue',
      description: 'Refund failed due to system error',
      gatewayResponse: 'Refund failed - insufficient funds',
      gatewayTransactionId: 'REF-GTW-FAIL123',
      createdAt: new Date(Date.now() - 14400000).toISOString(), // 4 hours ago
      updatedAt: new Date(Date.now() - 14400000).toISOString(),
      completedAt: undefined,
    },
  ];

  const loadRefunds = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // TODO: Replace with real API call
      // const refundsData = await dashboardAPI.getRefunds();
      
      // Get refunds from localStorage (real data from payment refunds)
      const storedRefunds = JSON.parse(localStorage.getItem('refunds') || '[]');
      
      // If no stored refunds, use initial mock data
      let refundsToShow = storedRefunds.length > 0 ? storedRefunds : initialMockRefunds;
      
      // Sort by creation date (newest first)
      refundsToShow.sort((a: RefundDetail, b: RefundDetail) => 
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
      
      setRefunds(refundsToShow);
    } catch (err: any) {
      setError(err.message || 'Failed to load refunds');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRefunds();
  }, []);

  const handleRefresh = () => {
    loadRefunds();
  };

  const handleViewRefund = (refundId: string) => {
    navigate(`/dashboard/refunds/${refundId}`);
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  const formatAmount = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'MMM dd, yyyy HH:mm');
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
        <Button onClick={loadRefunds}>
          Try Again
        </Button>
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          Refunds
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={handleRefresh}
          >
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/dashboard/payments')}
          >
            New Refund
          </Button>
        </Box>
      </Box>

      {/* Stats Cards */}
      <Box sx={{ 
        display: 'grid', 
        gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' }, 
        gap: 3, 
        mb: 4 
      }}>
        <Card>
          <CardContent>
            <Typography variant="h6" color="text.secondary" gutterBottom>
              Total Refunds
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
              {refunds.length}
            </Typography>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent>
            <Typography variant="h6" color="text.secondary" gutterBottom>
              Completed
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 'bold', color: 'success.main' }}>
              {refunds.filter(r => r.status === RefundStatus.COMPLETED).length}
            </Typography>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent>
            <Typography variant="h6" color="text.secondary" gutterBottom>
              Pending
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 'bold', color: 'warning.main' }}>
              {refunds.filter(r => r.status === RefundStatus.PENDING).length}
            </Typography>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent>
            <Typography variant="h6" color="text.secondary" gutterBottom>
              Failed
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 'bold', color: 'error.main' }}>
              {refunds.filter(r => r.status === RefundStatus.FAILED).length}
            </Typography>
          </CardContent>
        </Card>
      </Box>

      {/* Refunds Table */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom sx={{ mb: 3 }}>
            Recent Refunds
          </Typography>
          
          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Refund ID</TableCell>
                  <TableCell>Payment ID</TableCell>
                  <TableCell>Customer</TableCell>
                  <TableCell>Amount</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Reason</TableCell>
                  <TableCell>Created</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {refunds.map((refund) => (
                  <TableRow key={refund.id} hover>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                          {refund.refundId}
                        </Typography>
                        <Tooltip title="Copy">
                          <IconButton 
                            size="small" 
                            onClick={() => copyToClipboard(refund.refundId)}
                          >
                            <CopyIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                    
                    <TableCell>
                      <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                        {refund.paymentId}
                      </Typography>
                    </TableCell>
                    
                    <TableCell>
                      <Typography variant="body2">
                        {refund.customerId}
                      </Typography>
                    </TableCell>
                    
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {formatAmount(refund.amount, refund.currency)}
                      </Typography>
                    </TableCell>
                    
                    <TableCell>
                      <StatusChip status={refund.status} />
                    </TableCell>
                    
                    <TableCell>
                      <Typography variant="body2" sx={{ maxWidth: 200 }}>
                        {refund.reason}
                      </Typography>
                    </TableCell>
                    
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {formatDate(refund.createdAt)}
                      </Typography>
                    </TableCell>
                    
                    <TableCell>
                      <Tooltip title="View Details">
                        <IconButton 
                          size="small" 
                          onClick={() => handleViewRefund(refund.refundId)}
                        >
                          <VisibilityIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          
          {refunds.length === 0 && (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="body1" color="text.secondary">
                No refunds found
              </Typography>
            </Box>
          )}
        </CardContent>
      </Card>
    </Box>
  );
};

export default RefundsPage;
