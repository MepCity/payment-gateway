import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
  Typography,
  Alert,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import { ContentCopy, Warning } from '@mui/icons-material';
import { PaymentDetail } from '../../types/dashboard';

interface RefundModalProps {
  open: boolean;
  onClose: () => void;
  payment: PaymentDetail;
  onSubmit: (amount: number, reason: string) => void;
}

const RefundModal: React.FC<RefundModalProps> = ({
  open,
  onClose,
  payment,
  onSubmit,
}) => {
  const [refundAmount, setRefundAmount] = useState<string>('');
  const [reason, setReason] = useState<string>('');
  const [error, setError] = useState<string>('');

  const handleSubmit = () => {
    if (!refundAmount || !reason) {
      setError('Please fill in all required fields');
      return;
    }

    const amount = parseFloat(refundAmount);
    if (isNaN(amount) || amount <= 0) {
      setError('Please enter a valid refund amount');
      return;
    }

    if (amount > payment.amount) {
      setError('Refund amount cannot exceed payment amount');
      return;
    }

    onSubmit(amount, reason);
    handleClose();
  };

  const handleClose = () => {
    setRefundAmount('');
    setReason('');
    setError('');
    onClose();
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">Initiate Refund</Typography>
          <Chip 
            label="SUCCEEDED" 
            color="success" 
            size="small"
          />
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
          <Warning color="error" fontSize="small" />
          <Typography variant="body2" color="error">
            Note: Refunds cannot be canceled once placed. Please verify before proceeding.
          </Typography>
        </Box>
      </DialogTitle>

      <DialogContent>
        <Box sx={{ mb: 3 }}>
          <Box sx={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(2, 1fr)', 
            gap: 2, 
            mb: 3 
          }}>
            {/* Left Column */}
            <Box>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Amount
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                ${payment.amount.toFixed(2)} {payment.currency}
              </Typography>
            </Box>

            <Box>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Payment ID
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                  {payment.paymentId}
                </Typography>
                <Tooltip title="Copy">
                  <IconButton 
                    size="small" 
                    onClick={() => copyToClipboard(payment.paymentId)}
                  >
                    <ContentCopy fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Box>
            </Box>

            <Box>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Customer ID
              </Typography>
              <Typography variant="body2">
                {payment.customerId || 'N/A'}
              </Typography>
            </Box>

            <Box>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Customer Email
              </Typography>
              <Typography variant="body2">
                {payment.customerId ? `${payment.customerId}@example.com` : 'N/A'}
              </Typography>
            </Box>

            <Box>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Amount Refunded
              </Typography>
              <Typography variant="body2">
                $0.00 {payment.currency}
              </Typography>
            </Box>

            <Box>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Pending Requested Amount
              </Typography>
              <Typography variant="body2">
                $0.00 {payment.currency}
              </Typography>
            </Box>
          </Box>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="Refund Amount *"
            placeholder="Enter Refund Amount"
            type="number"
            value={refundAmount}
            onChange={(e) => setRefundAmount(e.target.value)}
            fullWidth
            required
            inputProps={{
              step: 0.01,
              min: 0,
              max: payment.amount,
            }}
          />

          <TextField
            label="Reason"
            placeholder="Enter Refund Reason"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            fullWidth
            multiline
            rows={3}
          />
        </Box>
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        <Button onClick={handleClose} variant="outlined">
          Cancel
        </Button>
        <Button 
          onClick={handleSubmit} 
          variant="contained" 
          color="primary"
          disabled={!refundAmount || !reason}
        >
          Initiate Refund
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default RefundModal;

