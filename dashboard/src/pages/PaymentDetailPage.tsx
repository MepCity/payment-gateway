import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Chip,
  Divider,
  Alert,
  CircularProgress,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Tabs,
  Tab,
} from '@mui/material';
import {
  ArrowBack,
  Sync,
  ExpandMore,
  Info,
  Warning,
  Error as ErrorIcon,
  CheckCircle,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { useAuth } from '../contexts/AuthContext';
import { dashboardAPI } from '../services/dashboardApi';
import { PaymentDetail, PaymentEvent, PaymentLog, PaymentStatus, PaymentMethod } from '../types/dashboard';
import StatusChip from '../components/common/StatusChip';
import RefundModal from '../components/payments/RefundModal';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index, ...other }) => {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
};

const PaymentDetailPage: React.FC = () => {
  const { paymentId } = useParams<{ paymentId: string }>();
  const navigate = useNavigate();
  const { state: authState } = useAuth();
  
  const [payment, setPayment] = useState<PaymentDetail | null>(null);
  const [events, setEvents] = useState<PaymentEvent[]>([]);
  const [logs, setLogs] = useState<PaymentLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);
  const [refundModalOpen, setRefundModalOpen] = useState(false);

  // Mock payment detail data
  const mockPaymentDetail: PaymentDetail = {
    id: 1,
    paymentId: paymentId || 'pay_O7H7KL7PWnc92drRvE8Z',
    transactionId: 'txn_123456789',
    merchantId: 'MERCH001',
    customerId: 'CUST001',
    amount: 23.00,
    currency: 'USD',
    status: PaymentStatus.COMPLETED,
    paymentMethod: PaymentMethod.CREDIT_CARD,
    cardNumber: '************1234',
    cardHolderName: 'John Doe',
    description: 'Test payment',
    gatewayResponse: 'Payment processed successfully',
    gatewayTransactionId: 'GTW-X1Y2Z3W4',
    cardBrand: 'VISA',
    cardBin: '411111',
    cardLastFour: '1234',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    completedAt: new Date().toISOString(),
  };

  const mockEvents: PaymentEvent[] = [
    {
      id: '1',
      eventType: 'PAYMENT_INITIATED',
      timestamp: new Date(Date.now() - 300000).toISOString(),
      status: 'INFO',
      message: 'Payment request received',
      details: { 
        merchantId: 'MERCH001',
        amount: 44.00,
        currency: 'USD'
      }
    },
    {
      id: '2',
      eventType: 'PAYMENT_PROCESSING',
      timestamp: new Date(Date.now() - 240000).toISOString(),
      status: 'INFO',
      message: 'Payment being processed by gateway',
      details: { 
        gateway: 'Garanti BBVA',
        processingTime: 150
      }
    },
    {
      id: '3',
      eventType: 'PAYMENT_COMPLETED',
      timestamp: new Date(Date.now() - 180000).toISOString(),
      status: 'SUCCESS',
      message: 'Payment completed successfully',
      details: { 
        authCode: 'AUTH123456',
        transactionId: 'TXN789012'
      }
    }
  ];

  const mockLogs: PaymentLog[] = [
    {
      id: '1',
      level: 'INFO',
      timestamp: new Date(Date.now() - 300000).toISOString(),
      message: 'POST /v1/payments - Payment Create',
      source: 'API',
      requestId: '8190bcae-d129-7640-bd7a-06af4cc96b72',
      merchantId: 'MERCH001',
      paymentId: paymentId || 'pay_O7H7KL7PWnc92drRvE8Z',
      apiAuthType: 'merchant_jwt',
      latency: 78,
      urlPath: '/payments',
      details: {
        httpStatus: 200,
        requestSize: 1024,
        responseSize: 512
      }
    },
    {
      id: '2',
      level: 'INFO',
      timestamp: new Date(Date.now() - 240000).toISOString(),
      message: 'Orca Elements Called',
      source: 'SDK',
      latency: 45,
      details: {
        elementType: 'payment_form',
        browserInfo: 'Chrome 91.0.4472.124'
      }
    },
    {
      id: '3',
      level: 'INFO',
      timestamp: new Date(Date.now() - 180000).toISOString(),
      message: 'App Rendered',
      source: 'SDK',
      latency: 12,
      details: {
        renderTime: 12,
        elementsLoaded: 5
      }
    }
  ];

  const loadPaymentDetail = async () => {
    if (!paymentId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      // Load real payment data from localStorage
      const storedPayments = JSON.parse(localStorage.getItem('payments') || '[]');
      const foundPayment = storedPayments.find((p: any) => 
        p.paymentId === paymentId || p.id === paymentId
      );
      
      if (foundPayment) {
        // Create PaymentDetail object from localStorage data with real status
        const realPaymentDetail: PaymentDetail = {
          id: foundPayment.id || 1,
          paymentId: foundPayment.paymentId || paymentId,
          transactionId: foundPayment.transactionId || `txn_${Date.now()}`,
          merchantId: foundPayment.merchantId || 'merchant_default',
          customerId: foundPayment.customerId || 'customer_default',
          amount: foundPayment.amount || 0,
          currency: foundPayment.currency || 'USD',
          status: foundPayment.status === 'COMPLETED' ? PaymentStatus.COMPLETED : PaymentStatus.PENDING,
          paymentMethod: foundPayment.paymentMethod || 'CREDIT_CARD',
          cardNumber: foundPayment.cardNumber || '************1234',
          cardHolderName: foundPayment.cardHolderName || 'N/A',
          description: foundPayment.description || 'Payment from customer',
          gatewayResponse: 'Success',
          gatewayTransactionId: foundPayment.transactionId || `gtxn_${Date.now()}`,
          createdAt: foundPayment.createdAt || new Date().toISOString(),
          updatedAt: foundPayment.updatedAt || new Date().toISOString(),
          completedAt: foundPayment.status === 'COMPLETED' ? foundPayment.createdAt || new Date().toISOString() : undefined,
        };
        
        console.log('Loaded real payment detail:', realPaymentDetail);
        setPayment(realPaymentDetail);
        
        // Set mock events and logs for now
        setEvents(mockEvents);
        setLogs(mockLogs);
      } else {
        // No payment found - show error
        setError('Payment not found');
        setPayment(null);
      }
    } catch (error) {
      console.error('Error loading payment detail:', error);
      setError('Failed to load payment details');
      setPayment(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPaymentDetail();
  }, [paymentId]);

  const handleSyncPayment = async () => {
    if (!paymentId) return;
    
    try {
      // TODO: Implement sync
      // await dashboardAPI.syncPaymentStatus(paymentId);
      console.log('Syncing payment:', paymentId);
      loadPaymentDetail();
    } catch (err: any) {
      setError(err.message || 'Failed to sync payment');
    }
  };

  const handleRefund = async (amount: number, reason: string) => {
    if (!paymentId) return;
    
    try {
      // TODO: Implement refund API call
      // await dashboardAPI.createRefund(paymentId, { amount, reason });
      console.log('Creating refund:', { paymentId, amount, reason });
      
      // Create refund object and store in localStorage for demo purposes
      // In real app, this would be stored in database
      const newRefund = {
        id: Date.now(), // Generate unique ID
        refundId: `ref_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        paymentId: paymentId,
        merchantId: payment?.merchantId || 'MERCH001',
        customerId: payment?.customerId || 'CUST001',
        amount: amount,
        currency: payment?.currency || 'USD',
        status: 'PENDING' as const,
        reason: reason,
        description: `Refund for payment ${paymentId}`,
        gatewayResponse: 'Refund request submitted',
        gatewayTransactionId: `REF-${payment?.gatewayTransactionId || 'GTW'}-${Date.now()}`,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        completedAt: null,
      };
      
      // Get existing refunds from localStorage
      const existingRefunds = JSON.parse(localStorage.getItem('refunds') || '[]');
      
      // Add new refund
      const updatedRefunds = [newRefund, ...existingRefunds];
      
      // Save back to localStorage
      localStorage.setItem('refunds', JSON.stringify(updatedRefunds));
      
      // Show success message
      alert(`Refund of $${amount} initiated successfully`);
      
      // Reload payment details to show updated status
      loadPaymentDetail();
    } catch (err: any) {
      setError(err.message || 'Failed to create refund');
    }
  };

  const formatAmount = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'MMM dd, yyyy HH:mm:ss');
  };

  const getEventIcon = (status: string) => {
    switch (status) {
      case 'SUCCESS':
        return <CheckCircle color="success" />;
      case 'FAILED':
        return <ErrorIcon color="error" />;
      case 'INFO':
        return <Info color="info" />;
      default:
        return <Warning color="warning" />;
    }
  };

  const getLogLevelColor = (level: string) => {
    switch (level) {
      case 'ERROR':
        return 'error';
      case 'WARN':
        return 'warning';
      case 'INFO':
      default:
        return 'info';
    }
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
        <Button onClick={() => navigate('/dashboard/payments')}>
          Back to Payments
        </Button>
      </Box>
    );
  }

  if (!payment) {
    return (
      <Box>
        <Alert severity="info">
          Payment not found
        </Alert>
        <Button onClick={() => navigate('/dashboard/payments')}>
          Back to Payments
        </Button>
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Button
            startIcon={<ArrowBack />}
            onClick={() => navigate('/dashboard/payments')}
            sx={{ mb: 1 }}
          >
            Payments
          </Button>
          <Typography variant="h4" gutterBottom>
            {payment.paymentId}
          </Typography>
        </Box>
        
        <Button
          variant="contained"
          startIcon={<Sync />}
          onClick={handleSyncPayment}
        >
          Sync
        </Button>
      </Box>

      {/* Summary and About Payment */}
      <Box sx={{ 
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
        gap: 3,
        mb: 4 
      }}>
        {/* Summary */}
        <Box>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
                <Typography variant="h3" sx={{ fontWeight: 'bold' }}>
                  {formatAmount(payment.amount, payment.currency)}
                </Typography>
                <StatusChip status={payment.status} size="medium" />
                {payment.status === PaymentStatus.COMPLETED && (
                  <Button
                    variant="outlined"
                    color="primary"
                    onClick={() => setRefundModalOpen(true)}
                    sx={{ ml: 'auto' }}
                  >
                    + Refund
                  </Button>
                )}
              </Box>
              
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                Summary
              </Typography>
              
              <Box sx={{ 
                display: 'grid',
                gridTemplateColumns: 'repeat(2, 1fr)',
                gap: 2 
              }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Created
                  </Typography>
                  <Typography variant="body2">
                    {formatDate(payment.createdAt)}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Last Updated
                  </Typography>
                  <Typography variant="body2">
                    {formatDate(payment.updatedAt)}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Amount Received
                  </Typography>
                  <Typography variant="body2">
                    {payment.status === PaymentStatus.COMPLETED ? formatAmount(payment.amount, payment.currency) : '$0.00'}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Payment ID
                  </Typography>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                    {payment.paymentId}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Connector Transaction ID
                  </Typography>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                    {payment.gatewayTransactionId || 'N/A'}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Error Message
                  </Typography>
                  <Typography variant="body2">
                    {payment.status === PaymentStatus.FAILED ? 'Payment failed' : 'N/A'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Box>

        {/* About Payment */}
        <Box>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                About Payment
              </Typography>
              
              <Box sx={{ 
                display: 'grid',
                gridTemplateColumns: 'repeat(2, 1fr)',
                gap: 2 
              }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Profile Id
                  </Typography>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                    {payment.merchantId}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Profile Name
                  </Typography>
                  <Typography variant="body2">
                    default
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Preferred connector
                  </Typography>
                  <Typography variant="body2">
                    NA
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Connector Label
                  </Typography>
                  <Typography variant="body2">
                    NA
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Payment Method Type
                  </Typography>
                  <Typography variant="body2">
                    NA
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Payment Method
                  </Typography>
                  <Typography variant="body2">
                    {payment.paymentMethod.replace('_', ' ')}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Auth Type
                  </Typography>
                  <Typography variant="body2">
                    three_ds
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Card Network
                  </Typography>
                  <Typography variant="body2">
                    {payment.cardBrand || 'NA'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Box>
      </Box>

      {/* Events and Logs */}
      <Accordion defaultExpanded>
        <AccordionSummary expandIcon={<ExpandMore />}>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            Events and logs
          </Typography>
        </AccordionSummary>
        <AccordionDetails>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={tabValue} onChange={(e, newValue) => setTabValue(newValue)}>
              <Tab label="Log Details" />
              <Tab label="Request" />
              <Tab label="Response" />
            </Tabs>
          </Box>
          
          <TabPanel value={tabValue} index={0}>
            {/* Events Timeline */}
            <Box sx={{ mb: 3 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                API Events
              </Typography>
              
              <Box sx={{ pl: 2 }}>
                {events.map((event, index) => (
                  <Box key={event.id} sx={{ display: 'flex', gap: 2, mb: 2 }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                      {getEventIcon(event.status)}
                      {index < events.length - 1 && (
                        <Box sx={{ width: 2, height: 40, bgcolor: 'divider', mt: 1 }} />
                      )}
                    </Box>
                    
                    <Box sx={{ flex: 1 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                        <Chip 
                          label="200" 
                          size="small" 
                          color="success" 
                          variant="outlined"
                        />
                        <Typography variant="body2" sx={{ fontWeight: 500 }}>
                          POST
                        </Typography>
                        <Typography variant="body2">
                          {event.eventType.replace('_', ' ')}
                        </Typography>
                      </Box>
                      
                      <Typography variant="caption" color="text.secondary">
                        {formatDate(event.timestamp)}
                      </Typography>
                    </Box>
                  </Box>
                ))}
              </Box>
            </Box>

            {/* Logs Table */}
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                Request Logs
              </Typography>
              
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Merchant Id</TableCell>
                      <TableCell>Payment Id</TableCell>
                      <TableCell>Request Id</TableCell>
                      <TableCell>Api Auth Type</TableCell>
                      <TableCell>Authentication Data</TableCell>
                      <TableCell>Latency</TableCell>
                      <TableCell>Url Path</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {logs.map((log) => (
                      <TableRow key={log.id}>
                        <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>
                          {log.merchantId || 'N/A'}
                        </TableCell>
                        <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>
                          {log.paymentId || 'N/A'}
                        </TableCell>
                        <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>
                          {log.requestId || 'N/A'}
                        </TableCell>
                        <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>
                          {log.apiAuthType || 'N/A'}
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                            {JSON.stringify(log.details || {}, null, 2)}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          {log.latency || 'N/A'}
                        </TableCell>
                        <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>
                          {log.urlPath || 'N/A'}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          </TabPanel>
          
          <TabPanel value={tabValue} index={1}>
            <Typography variant="body2">
              Request data will be displayed here...
            </Typography>
          </TabPanel>
          
          <TabPanel value={tabValue} index={2}>
            <Typography variant="body2">
              Response data will be displayed here...
            </Typography>
          </TabPanel>
        </AccordionDetails>
      </Accordion>

      {/* Refund Modal */}
      {payment && (
        <RefundModal
          open={refundModalOpen}
          onClose={() => setRefundModalOpen(false)}
          payment={payment}
          onSubmit={handleRefund}
        />
      )}
    </Box>
  );
};

export default PaymentDetailPage;
