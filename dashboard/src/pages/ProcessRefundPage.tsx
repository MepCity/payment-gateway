import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  MenuItem,
  Alert,
  CircularProgress,
  Divider,
} from '@mui/material';
import {
  ArrowBack,
  Receipt,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { dashboardAPI } from '../services/dashboardApi';
import { RefundReason } from '../types/dashboard';

interface RefundFormData {
  paymentId: string;
  transactionId: string;
  merchantId: string;
  customerId: string;
  amount: number;
  currency: string;
  reason: RefundReason;
  description: string;
}

const ProcessRefundPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { state: authState } = useAuth();
  
  const [formData, setFormData] = useState<RefundFormData>({
    paymentId: '',
    transactionId: '',
    merchantId: authState.user?.merchantId || '',
    customerId: '',
    amount: 0,
    currency: 'USD',
    reason: RefundReason.CUSTOMER_REQUEST,
    description: '',
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [paymentData, setPaymentData] = useState<any>(null);

  // Populate form data from location state (when coming from payment details)
  useEffect(() => {
    if (location.state) {
      const stateData = location.state as {
        paymentId?: string;
        originalAmount?: number;
        currency?: string;
        customerId?: string;
        cardHolderName?: string;
        paymentMethod?: string;
      };
      
      if (stateData.paymentId) {
        // Fetch payment details to get transaction ID
        fetchPaymentDetails(stateData.paymentId);
        
        setFormData(prev => ({
          ...prev,
          paymentId: stateData.paymentId || '',
          customerId: stateData.customerId || '',
          currency: stateData.currency || 'USD',
          // Leave amount empty for user to enter (can be partial refund)
        }));
      }
    }
  }, [location.state]);

  const fetchPaymentDetails = async (paymentId: string) => {
    try {
      const payment = await dashboardAPI.getPaymentDetail(paymentId);
      setPaymentData(payment);
      setFormData(prev => ({
        ...prev,
        transactionId: payment.transactionId || '',
        customerId: payment.customerId || prev.customerId,
        currency: payment.currency || prev.currency,
      }));
    } catch (err) {
      console.error('Error fetching payment details:', err);
    }
  };

  const handleInputChange = (field: keyof RefundFormData) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const value = field === 'amount' ? parseFloat(event.target.value) || 0 : event.target.value;
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.paymentId || !formData.transactionId || !formData.customerId || formData.amount <= 0) {
      setError('Please fill in all required fields');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);
    
    try {
      const response = await dashboardAPI.createRefund({
        paymentId: formData.paymentId,
        transactionId: formData.transactionId,
        merchantId: formData.merchantId,
        customerId: formData.customerId,
        amount: formData.amount,
        currency: formData.currency,
        reason: formData.reason,
        description: formData.description || undefined,
      });
      
      if (response.success) {
        setSuccess(`Refund created successfully: ${response.refundId}`);
        
        // Reset form
        setFormData({
          paymentId: '',
          transactionId: '',
          merchantId: authState.user?.merchantId || '',
          customerId: '',
          amount: 0,
          currency: 'USD',
          reason: RefundReason.CUSTOMER_REQUEST,
          description: '',
        });
        
        // Redirect to refund details after 2 seconds
        setTimeout(() => {
          navigate(`/dashboard/refunds/${response.refundId}`);
        }, 2000);
      } else {
        setError(response.message || 'Failed to create refund');
      }
    } catch (err: any) {
      console.error('Create refund error:', err);
      setError(err.response?.data?.message || err.message || 'Failed to create refund');
    } finally {
      setLoading(false);
    }
  };

  const formatAmount = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  };

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Button
            startIcon={<ArrowBack />}
            onClick={() => navigate('/dashboard/refunds')}
            sx={{ mb: 1 }}
          >
            Refunds
          </Button>
          <Typography variant="h4" gutterBottom>
            Process Refund
          </Typography>
        </Box>
      </Box>

      {/* Error Alert */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Success Alert */}
      {success && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      {/* Form */}
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
            <Receipt sx={{ fontSize: 32, color: 'primary.main' }} />
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              Refund Details
            </Typography>
          </Box>

          <Box component="form" onSubmit={handleSubmit}>
            <Box sx={{ 
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
              gap: 3,
              mb: 3
            }}>
              {/* Payment Information */}
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                  Payment Information
                </Typography>
                
                <TextField
                  fullWidth
                  label="Payment ID"
                  value={formData.paymentId}
                  onChange={handleInputChange('paymentId')}
                  required
                  disabled={loading}
                  sx={{ mb: 2 }}
                />
                
                <TextField
                  fullWidth
                  label="Transaction ID"
                  value={formData.transactionId}
                  onChange={handleInputChange('transactionId')}
                  required
                  disabled={loading}
                  sx={{ mb: 2 }}
                />
                
                <TextField
                  fullWidth
                  label="Customer ID"
                  value={formData.customerId}
                  onChange={handleInputChange('customerId')}
                  required
                  disabled={loading}
                />
              </Box>

              {/* Refund Information */}
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                  Refund Information
                </Typography>
                
                <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                  <TextField
                    label="Amount"
                    type="number"
                    value={formData.amount}
                    onChange={handleInputChange('amount')}
                    required
                    disabled={loading}
                    inputProps={{ 
                      min: 0.01, 
                      step: 0.01,
                      max: 999999.99 
                    }}
                    sx={{ flex: 2 }}
                  />
                  
                  <TextField
                    select
                    label="Currency"
                    value={formData.currency}
                    onChange={handleInputChange('currency')}
                    disabled={loading}
                    sx={{ flex: 1 }}
                  >
                    <MenuItem value="USD">USD</MenuItem>
                    <MenuItem value="EUR">EUR</MenuItem>
                    <MenuItem value="TRY">TRY</MenuItem>
                    <MenuItem value="GBP">GBP</MenuItem>
                  </TextField>
                </Box>
                
                <TextField
                  select
                  fullWidth
                  label="Refund Reason"
                  value={formData.reason}
                  onChange={handleInputChange('reason')}
                  required
                  disabled={loading}
                  sx={{ mb: 2 }}
                >
                  {Object.values(RefundReason).map((reason) => (
                    <MenuItem key={reason} value={reason}>
                      {reason.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())}
                    </MenuItem>
                  ))}
                </TextField>
                
                <TextField
                  fullWidth
                  label="Description (Optional)"
                  multiline
                  rows={3}
                  value={formData.description}
                  onChange={handleInputChange('description')}
                  disabled={loading}
                  placeholder="Additional notes about the refund..."
                />
              </Box>
            </Box>

            <Divider sx={{ my: 3 }} />

            {/* Summary */}
            {formData.amount > 0 && (
              <Box sx={{ mb: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 1 }}>
                  Refund Summary
                </Typography>
                <Typography variant="body2">
                  Amount to refund: <strong>{formatAmount(formData.amount, formData.currency)}</strong>
                </Typography>
                <Typography variant="body2">
                  Reason: <strong>{formData.reason.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())}</strong>
                </Typography>
              </Box>
            )}

            {/* Actions */}
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
              <Button
                variant="outlined"
                onClick={() => navigate('/dashboard/refunds')}
                disabled={loading}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                variant="contained"
                disabled={loading || formData.amount <= 0}
                startIcon={loading ? <CircularProgress size={20} /> : null}
              >
                {loading ? 'Processing...' : 'Process Refund'}
              </Button>
            </Box>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default ProcessRefundPage;
