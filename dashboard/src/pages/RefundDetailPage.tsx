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
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  ArrowBack,
  Sync,
  ExpandMore,
  Info,
  Warning,
  Error as ErrorIcon,
  CheckCircle,
  ContentCopy,
  OpenInNew,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { RefundDetail, RefundStatus } from '../types/dashboard';
import StatusChip from '../components/common/StatusChip';

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

const RefundDetailPage: React.FC = () => {
  const { refundId } = useParams<{ refundId: string }>();
  const navigate = useNavigate();
  
  const [refund, setRefund] = useState<RefundDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);

  const loadRefundDetail = async () => {
    if (!refundId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      // Get refunds from localStorage
      const storedRefunds = JSON.parse(localStorage.getItem('refunds') || '[]');
      const foundRefund = storedRefunds.find((r: RefundDetail) => r.refundId === refundId);
      
      if (!foundRefund) {
        setError('Refund not found');
        return;
      }
      
      setRefund(foundRefund);
      
    } catch (err: any) {
      setError(err.message || 'Failed to load refund details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRefundDetail();
  }, [refundId]);

  const handleSyncRefund = async () => {
    if (!refundId) return;
    
    try {
      // TODO: Implement sync
      console.log('Syncing refund:', refundId);
      loadRefundDetail();
    } catch (err: any) {
      setError(err.message || 'Failed to sync refund');
    }
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
    return format(new Date(dateString), 'MMM dd, yyyy HH:mm:ss');
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
        <Button onClick={() => navigate('/dashboard/refunds')}>
          Back to Refunds
        </Button>
      </Box>
    );
  }

  if (!refund) {
    return (
      <Box>
        <Alert severity="info">
          Refund not found
        </Alert>
        <Button onClick={() => navigate('/dashboard/refunds')}>
          Back to Refunds
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
            onClick={() => navigate('/dashboard/refunds')}
            sx={{ mb: 1 }}
          >
            Refunds
          </Button>
          <Typography variant="h4" gutterBottom>
            {refund.refundId}
          </Typography>
        </Box>
        
        <Button
          variant="contained"
          startIcon={<Sync />}
          onClick={handleSyncRefund}
        >
          Sync
        </Button>
      </Box>

      {/* Summary and About Refund */}
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
                  {formatAmount(refund.amount, refund.currency)}
                </Typography>
                <StatusChip status={refund.status} size="medium" />
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
                    {formatDate(refund.createdAt)}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Last Updated
                  </Typography>
                  <Typography variant="body2">
                    {formatDate(refund.updatedAt)}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Amount Refunded
                  </Typography>
                  <Typography variant="body2">
                    {refund.status === RefundStatus.COMPLETED ? formatAmount(refund.amount, refund.currency) : '$0.00'}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Refund ID
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                      {refund.refundId}
                    </Typography>
                    <Tooltip title="Copy">
                      <IconButton 
                        size="small" 
                        onClick={() => copyToClipboard(refund.refundId)}
                      >
                        <ContentCopy fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Payment ID
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                      {refund.paymentId}
                    </Typography>
                    <Tooltip title="Copy">
                      <IconButton 
                        size="small" 
                        onClick={() => copyToClipboard(refund.paymentId)}
                      >
                        <ContentCopy fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="View Payment">
                      <IconButton 
                        size="small" 
                        onClick={() => navigate(`/dashboard/payments/${refund.paymentId}`)}
                      >
                        <OpenInNew fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Gateway Transaction ID
                  </Typography>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                    {refund.gatewayTransactionId || 'N/A'}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Error Message
                  </Typography>
                  <Typography variant="body2">
                    {refund.status === RefundStatus.FAILED ? refund.gatewayResponse || 'Refund failed' : 'N/A'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Box>

        {/* About Refund */}
        <Box>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                About Refund
              </Typography>
              
              <Box sx={{ 
                display: 'grid',
                gridTemplateColumns: 'repeat(2, 1fr)',
                gap: 2 
              }}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Merchant ID
                  </Typography>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                    {refund.merchantId}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Customer ID
                  </Typography>
                  <Typography variant="body2">
                    {refund.customerId}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Currency
                  </Typography>
                  <Typography variant="body2">
                    {refund.currency}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Status
                  </Typography>
                  <StatusChip status={refund.status} />
                </Box>
                
                <Box sx={{ gridColumn: 'span 2' }}>
                  <Typography variant="body2" color="text.secondary">
                    Reason
                  </Typography>
                  <Typography variant="body2">
                    {refund.reason}
                  </Typography>
                </Box>
                
                <Box sx={{ gridColumn: 'span 2' }}>
                  <Typography variant="body2" color="text.secondary">
                    Description
                  </Typography>
                  <Typography variant="body2">
                    {refund.description || 'N/A'}
                  </Typography>
                </Box>
                
                <Box sx={{ gridColumn: 'span 2' }}>
                  <Typography variant="body2" color="text.secondary">
                    Gateway Response
                  </Typography>
                  <Typography variant="body2">
                    {refund.gatewayResponse || 'N/A'}
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
            {/* Refund Timeline */}
            <Box sx={{ mb: 3 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                Refund Events
              </Typography>
              
              <Box sx={{ pl: 2 }}>
                <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                  <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                    <CheckCircle color="success" />
                    <Box sx={{ width: 2, height: 40, bgcolor: 'divider', mt: 1 }} />
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
                        Refund Create
                      </Typography>
                    </Box>
                    
                    <Typography variant="caption" color="text.secondary">
                      {formatDate(refund.createdAt)}
                    </Typography>
                  </Box>
                </Box>
                
                {refund.status === RefundStatus.COMPLETED && (
                  <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                      <CheckCircle color="success" />
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
                          Refund Completed
                        </Typography>
                      </Box>
                      
                      <Typography variant="caption" color="text.secondary">
                        {refund.completedAt ? formatDate(refund.completedAt) : 'N/A'}
                      </Typography>
                    </Box>
                  </Box>
                )}
              </Box>
            </Box>

            {/* Refund Logs */}
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                Refund Logs
              </Typography>
              
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Merchant ID</TableCell>
                      <TableCell>Refund ID</TableCell>
                      <TableCell>Payment ID</TableCell>
                      <TableCell>Amount</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Created</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    <TableRow>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>
                        {refund.merchantId}
                      </TableCell>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>
                        {refund.refundId}
                      </TableCell>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>
                        {refund.paymentId}
                      </TableCell>
                      <TableCell>
                        {formatAmount(refund.amount, refund.currency)}
                      </TableCell>
                      <TableCell>
                        <StatusChip status={refund.status} />
                      </TableCell>
                      <TableCell>
                        {formatDate(refund.createdAt)}
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          </TabPanel>
          
          <TabPanel value={tabValue} index={1}>
            <Typography variant="body2">
              Refund request data will be displayed here...
            </Typography>
          </TabPanel>
          
          <TabPanel value={tabValue} index={2}>
            <Typography variant="body2">
              Refund response data will be displayed here...
            </Typography>
          </TabPanel>
        </AccordionDetails>
      </Accordion>
    </Box>
  );
};

export default RefundDetailPage;

