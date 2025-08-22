import React from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Box,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Visibility,
  Sync,
  CreditCard,
  AccountBalance,
  Wallet,
  Replay,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { PaymentListItem, PaymentMethod } from '../../types/dashboard';
import StatusChip from '../common/StatusChip';

interface PaymentsTableProps {
  payments: PaymentListItem[];
  loading?: boolean;
  onRowClick: (payment: PaymentListItem) => void;
  onSyncPayment?: (paymentId: string) => void;
  onProcessAgain?: (payment: PaymentListItem) => void;
}

const PaymentsTable: React.FC<PaymentsTableProps> = ({
  payments,
  loading = false,
  onRowClick,
  onSyncPayment,
  onProcessAgain,
}) => {
  console.log('PaymentsTable received payments:', payments);
  const getPaymentMethodIcon = (method: PaymentMethod) => {
    switch (method) {
      case PaymentMethod.CREDIT_CARD:
      case PaymentMethod.DEBIT_CARD:
        return <CreditCard fontSize="small" />;
      case PaymentMethod.BANK_TRANSFER:
        return <AccountBalance fontSize="small" />;
      case PaymentMethod.DIGITAL_WALLET:
        return <Wallet fontSize="small" />;
      default:
        return <CreditCard fontSize="small" />;
    }
  };

  const formatAmount = (amount: number, currency: string) => {
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'MMM dd, yyyy HH:mm');
  };

  if (payments.length === 0 && !loading) {
    return (
      <Paper sx={{ 
        p: 4, 
        textAlign: 'center',
        backgroundColor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider'
      }}>
        <Typography variant="h6" color="text.secondary">
          No payments found
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Try adjusting your filters or date range
        </Typography>
      </Paper>
    );
  }

  return (
    <TableContainer component={Paper} sx={{ 
      borderRadius: 2,
      backgroundColor: 'background.paper',
      border: '1px solid',
      borderColor: 'divider'
    }}>
      <Table sx={{ minWidth: 650 }}>
        <TableHead>
          <TableRow sx={{ 
            backgroundColor: 'background.paper',
            borderBottom: '1px solid',
            borderColor: 'divider'
          }}>
            <TableCell sx={{ 
              fontWeight: 600,
              color: 'text.primary',
              backgroundColor: 'background.paper'
            }}>S.No</TableCell>
            <TableCell sx={{ 
              fontWeight: 600,
              color: 'text.primary',
              backgroundColor: 'background.paper'
            }}>Payment ID</TableCell>
            <TableCell sx={{ 
              fontWeight: 600,
              color: 'text.primary',
              backgroundColor: 'background.paper'
            }}>Transaction ID</TableCell>
            <TableCell sx={{ 
              fontWeight: 600,
              color: 'text.primary',
              backgroundColor: 'background.paper'
            }}>Connector</TableCell>
            <TableCell sx={{ 
              fontWeight: 600,
              color: 'text.primary',
              backgroundColor: 'background.paper'
            }}>Profile Id</TableCell>
            <TableCell sx={{ 
              fontWeight: 600,
              color: 'text.primary',
              backgroundColor: 'background.paper'
            }}>Amount</TableCell>
            <TableCell sx={{ 
              fontWeight: 600,
              color: 'text.primary',
              backgroundColor: 'background.paper'
            }}>Payment Status</TableCell>
            <TableCell sx={{ 
              fontWeight: 600,
              color: 'text.primary',
              backgroundColor: 'background.paper'
            }}>Payment Method</TableCell>
            <TableCell sx={{ 
              fontWeight: 600,
              color: 'text.primary',
              backgroundColor: 'background.paper'
            }}>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {payments.map((payment, index) => (
            <TableRow
              key={payment.paymentId}
              hover
              sx={{ 
                cursor: 'pointer',
                backgroundColor: 'background.paper',
                borderBottom: '1px solid',
                borderColor: 'divider',
                '&:hover': {
                  backgroundColor: 'action.hover',
                }
              }}
              onClick={() => onRowClick(payment)}
            >
              <TableCell>
                <Typography variant="body2">
                  {index + 1}
                </Typography>
              </TableCell>
              
              <TableCell>
                <Box>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace', fontWeight: 500 }}>
                    {payment.paymentId}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {formatDate(payment.createdAt)}
                  </Typography>
                </Box>
              </TableCell>

              <TableCell>
                <Typography variant="body2" sx={{ fontFamily: 'monospace', fontWeight: 500 }}>
                  {payment.transactionId || 'N/A'}
                </Typography>
              </TableCell>

              <TableCell>
                <Chip
                  label="NA"
                  size="small"
                  variant="outlined"
                  sx={{ borderRadius: 1 }}
                />
              </TableCell>

              <TableCell>
                <Chip
                  label="NA"
                  size="small"
                  variant="outlined"
                  sx={{ borderRadius: 1 }}
                />
              </TableCell>

              <TableCell>
                <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                  {payment.merchantId}
                </Typography>
              </TableCell>

              <TableCell>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  {formatAmount(payment.amount, payment.currency)}
                </Typography>
              </TableCell>

              <TableCell>
                <StatusChip status={payment.status} />
              </TableCell>

              <TableCell>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  {getPaymentMethodIcon(payment.paymentMethod)}
                  <Typography variant="body2">
                    {payment.paymentMethod.replace('_', ' ')}
                  </Typography>
                </Box>
              </TableCell>

              <TableCell>
                <Box sx={{ display: 'flex', gap: 0.5 }}>
                  <Tooltip title="View Details">
                    <IconButton 
                      size="small" 
                      onClick={(e) => {
                        e.stopPropagation();
                        onRowClick(payment);
                      }}
                    >
                      <Visibility fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  
                  {onProcessAgain && (
                    <Tooltip title="Process Again">
                      <IconButton 
                        size="small"
                        color="primary"
                        onClick={(e) => {
                          e.stopPropagation();
                          onProcessAgain(payment);
                        }}
                      >
                        <Replay fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  )}
                  
                  {onSyncPayment && (
                    <Tooltip title="Sync Status">
                      <IconButton 
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          onSyncPayment(payment.paymentId);
                        }}
                      >
                        <Sync fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  )}
                </Box>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default PaymentsTable;