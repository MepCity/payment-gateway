import React, { useState } from 'react';
import {
  Box,
  Typography,
  TextField,
  Button,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  CircularProgress,
  Tabs,
  Tab,
  Divider,
  Chip,
} from '@mui/material';
import {
  Payment,
  CreditCard,
  AccountBalance,
  Security,
  Sync
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { paymentApi, PaymentRequest } from '../services/paymentApi';

interface PaymentFormData {
  customerId: string;
  amount: string;
  currency: string;
  paymentMethod: string;
  cardNumber: string;
  cardHolderName: string;
  expiryDate: string;
  cvv: string;
  description: string;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && <Box>{children}</Box>}
    </div>
  );
}

const ProcessPaymentPage: React.FC = () => {
  const { state } = useAuth();
  const navigate = useNavigate();
  const [tabValue, setTabValue] = useState(0);
  const [formData, setFormData] = useState<PaymentFormData>({
    customerId: 'hyperswitch_sdk_demo_id',
    amount: '',
    currency: 'USD',
    paymentMethod: 'CREDIT_CARD',
    cardNumber: '',
    cardHolderName: '',
    expiryDate: '',
    cvv: '',
    description: '',
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<{
    message: string;
    paymentId: string;
    transactionId: string;
    amount: number;
    currency: string;
    status: string;
    cardLastFour?: string;
  } | null>(null);  
  const [showSavedCard, setShowSavedCard] = useState(true);

  // Test card data
  const testCards = [
    {
      name: 'Visa Test Card',
      cardNumber: '4242 4242 4242 4242',
      cardHolderName: 'John Doe',
      expiryDate: '12/25',
      cvv: '123',
      description: 'Visa test card - Always succeeds'
    },
    {
      name: 'Mastercard Test Card',
      cardNumber: '5555 5555 5555 4444',
      cardHolderName: 'Jane Smith',
      expiryDate: '03/26',
      cvv: '456',
      description: 'Mastercard test card - Always succeeds'
    },
    {
      name: 'Amex Test Card',
      cardNumber: '3782 822463 10005',
      cardHolderName: 'Test User',
      expiryDate: '09/27',
      cvv: '1234',
      description: 'American Express test card'
    }
  ];

  const fillTestCard = (testCard: typeof testCards[0]) => {
    setFormData(prev => ({
      ...prev,
      cardNumber: testCard.cardNumber,
      cardHolderName: testCard.cardHolderName,
      expiryDate: testCard.expiryDate,
      cvv: testCard.cvv
    }));
  };

  // Debug: Clear old localStorage if needed
  React.useEffect(() => {
    const apiKey = localStorage.getItem('auth_api_key');
    if (apiKey && !apiKey.startsWith('pk_test_')) {
      console.log('Clearing old API key:', apiKey);
      localStorage.clear();
      window.location.href = '/login';
    }
  }, []);

  const handleInputChange = (field: keyof PaymentFormData) => (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | 
    { target: { value: string } }
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value
    }));
  };

  const formatCardNumber = (value: string) => {
    // Remove all non-digit characters
    const digits = value.replace(/\D/g, '');
    // Add spaces every 4 digits
    const formatted = digits.replace(/(\d{4})(?=\d)/g, '$1 ');
    return formatted.substring(0, 19); // Max 16 digits + 3 spaces
  };

  const formatExpiryDate = (value: string) => {
    // Remove all non-digit characters
    const digits = value.replace(/\D/g, '');
    // Add slash after 2 digits
    if (digits.length >= 2) {
      return digits.substring(0, 2) + '/' + digits.substring(2, 4);
    }
    return digits;
  };

  const handleCardNumberChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatCardNumber(event.target.value);
    setFormData(prev => ({
      ...prev,
      cardNumber: formatted
    }));
  };

  const handleExpiryDateChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatExpiryDate(event.target.value);
    setFormData(prev => ({
      ...prev,
      expiryDate: formatted
    }));
  };

  const validateForm = (): boolean => {
    if (!formData.customerId.trim()) {
      setError('Customer ID is required');
      return false;
    }
    
    const amount = parseFloat(formData.amount);
    if (!formData.amount || isNaN(amount) || amount <= 0) {
      setError('Valid amount is required');
      return false;
    }
    
    if (!formData.cardNumber.replace(/\s/g, '')) {
      setError('Card number is required');
      return false;
    }
    
    if (!formData.cardHolderName.trim()) {
      setError('Card holder name is required');
      return false;
    }
    
    if (!formData.expiryDate || formData.expiryDate.length !== 5) {
      setError('Valid expiry date is required (MM/YY)');
      return false;
    }
    
    if (!formData.cvv || formData.cvv.length < 3) {
      setError('Valid CVV is required');
      return false;
    }
    
    return true;
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);
    setError(null);
    setSuccess(null);
    
    try {
      // Prepare payment data
      const paymentData: PaymentRequest = {
        merchantId: state.user?.merchantId || '',
        customerId: formData.customerId,
        amount: parseFloat(formData.amount),
        currency: formData.currency,
        paymentMethod: formData.paymentMethod as 'CREDIT_CARD' | 'DEBIT_CARD',
        cardNumber: formData.cardNumber.replace(/\s/g, ''),
        cardHolderName: formData.cardHolderName,
        expiryDate: formData.expiryDate,
        cvv: formData.cvv,
        description: formData.description,
      };

      console.log('Processing payment:', paymentData);
      console.log('API Key from localStorage:', localStorage.getItem('auth_api_key'));
      console.log('Token from localStorage:', localStorage.getItem('auth_token'));
      
      // Backend is working, skip health check
      
      // Call real backend API
      const response = await paymentApi.createPayment(paymentData);
      
      if (response.success) {
        // Save customer information to localStorage
        if (formData.cardHolderName.trim()) {
          const newCustomer = {
            id: Date.now(),
            customerId: formData.customerId || `customer_${Date.now()}`,
            customerName: formData.cardHolderName.trim(),
            email: formData.customerId.includes('@') ? formData.customerId : 'guest@example.com',
            phoneCountryCode: undefined,
            phone: undefined,
            description: formData.description || 'Customer from payment',
            address: undefined,
            status: 'ACTIVE' as const,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            lastPaymentAt: new Date().toISOString(),
            totalPayments: 1,
            totalAmount: parseFloat(formData.amount),
            currency: formData.currency,
          };

          // Get existing customers from localStorage
          const existingCustomers = JSON.parse(localStorage.getItem('customers') || '[]');
          
          // Check if customer already exists
          const existingCustomerIndex = existingCustomers.findIndex(
            (c: any) => c.customerId === newCustomer.customerId
          );

          if (existingCustomerIndex >= 0) {
            // Update existing customer
            existingCustomers[existingCustomerIndex] = {
              ...existingCustomers[existingCustomerIndex],
              totalPayments: existingCustomers[existingCustomerIndex].totalPayments + 1,
              totalAmount: existingCustomers[existingCustomerIndex].totalAmount + parseFloat(formData.amount),
              lastPaymentAt: new Date().toISOString(),
              updatedAt: new Date().toISOString(),
            };
          } else {
            // Add new customer
            existingCustomers.push(newCustomer);
          }

          // Save back to localStorage
          localStorage.setItem('customers', JSON.stringify(existingCustomers));
        }

        // Save payment information to localStorage for customer detail page
        const paymentData = {
          id: Date.now(),
          paymentId: response.paymentId,
          transactionId: response.transactionId,
          merchantId: state.user?.merchantId || 'merchant_default',
          customerId: formData.customerId,
          amount: parseFloat(formData.amount),
          currency: formData.currency,
          status: 'COMPLETED', // Changed from 'SUCCEEDED' to 'COMPLETED'
          paymentMethod: formData.paymentMethod,
          cardNumber: formData.cardNumber.replace(/\s/g, ''),
          cardHolderName: formData.cardHolderName,
          expiryDate: formData.expiryDate,
          description: formData.description,
          createdAt: new Date().toISOString()
        };

        console.log('Saving payment data to localStorage:', paymentData);

        // Get existing payments from localStorage
        const existingPayments = JSON.parse(localStorage.getItem('payments') || '[]');
        existingPayments.push(paymentData);
        localStorage.setItem('payments', JSON.stringify(existingPayments));
        
        console.log('Payment data saved to localStorage. Total payments:', existingPayments.length);
        console.log('localStorage payments:', localStorage.getItem('payments'));

        setSuccess({
          message: 'Payment processed successfully!',
          paymentId: response.paymentId,
          transactionId: response.transactionId,
          amount: response.amount,
          currency: response.currency,
          status: response.status,
          cardLastFour: response.cardLastFour
        });
        
        // Reset form
        setFormData({
          customerId: '',
          amount: '',
          currency: 'TRY',
          paymentMethod: 'CREDIT_CARD',
          cardNumber: '',
          cardHolderName: '',
          expiryDate: '',
          cvv: '',
          description: '',
        });
      } else {
        setError(response.message || 'Payment processing failed');
      }
      
    } catch (err: any) {
      console.error('Payment error:', err);
      setError(err.message || 'Payment processing failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 600, mb: 1, display: 'flex', alignItems: 'center', gap: 2 }}>
          <Payment color="primary" />
          Process Payment
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Process a new payment transaction securely
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert 
          severity="success" 
          sx={{ mb: 3 }} 
          onClose={() => setSuccess(null)}
          action={
            <Button
              color="inherit"
              size="small"
              onClick={() => navigate('/dashboard/payments')}
              sx={{ textTransform: 'none' }}
            >
              View in Dashboard
            </Button>
          }
        >
          <Box>
            <Typography variant="body1" sx={{ mb: 1 }}>
              {success.message}
            </Typography>
            <Typography variant="body2" sx={{ mb: 1 }}>
              Amount: {success.amount} {success.currency}
            </Typography>
            <Typography variant="body2" sx={{ mb: 1 }}>
              Payment ID: {success.paymentId}
            </Typography>
            <Typography variant="body2" sx={{ mb: 1 }}>
              Status: {success.status}
            </Typography>
          </Box>
        </Alert>
      )}

      <Card>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <Box sx={{ 
              mb: 3,
              p: 3, 
              bgcolor: 'background.paper', 
              borderRadius: 2,
              border: '1px solid',
              borderColor: 'divider'
            }}>
              <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                <AccountBalance />
                Transaction Details
              </Typography>
              
              <Box sx={{ 
                display: 'grid',
                gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' },
                gap: 3 
              }}>
                <Box>
                  <TextField
                    fullWidth
                    label="Customer ID"
                    value={formData.customerId}
                    onChange={handleInputChange('customerId')}
                    required
                    disabled={loading}
                  />
                </Box>
                
                <Box>
                  <TextField
                    fullWidth
                    label="Amount"
                    type="number"
                    value={formData.amount}
                    onChange={handleInputChange('amount')}
                    required
                    disabled={loading}
                    inputProps={{ step: '0.01', min: '0.01' }}
                  />
                </Box>
                
                <Box>
                  <FormControl fullWidth>
                    <InputLabel>Currency</InputLabel>
                    <Select
                      value={formData.currency}
                      onChange={handleInputChange('currency')}
                      disabled={loading}
                    >
                      <MenuItem value="TRY">TRY - Turkish Lira</MenuItem>
                      <MenuItem value="USD">USD - US Dollar</MenuItem>
                      <MenuItem value="EUR">EUR - Euro</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
                
                <Box>
                  <FormControl fullWidth>
                    <InputLabel>Payment Method</InputLabel>
                    <Select
                      value={formData.paymentMethod}
                      onChange={handleInputChange('paymentMethod')}
                      disabled={loading}
                    >
                      <MenuItem value="CREDIT_CARD">Credit Card</MenuItem>
                      <MenuItem value="DEBIT_CARD">Debit Card</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
                
                <Box sx={{ gridColumn: { xs: '1', sm: '1 / -1' } }}>
                  <TextField
                    fullWidth
                    label="Description (Optional)"
                    value={formData.description}
                    onChange={handleInputChange('description')}
                    disabled={loading}
                    multiline
                    rows={2}
                  />
                </Box>
              </Box>
            </Box>

            <Divider sx={{ my: 3 }} />

            <Box sx={{ 
              p: 3, 
              bgcolor: 'background.paper', 
              borderRadius: 2,
              border: '1px solid',
              borderColor: 'divider'
            }}>
              <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                <CreditCard />
                Card Information
              </Typography>
              
              <Box sx={{ 
                display: 'grid',
                gridTemplateColumns: '1fr',
                gap: 3 
              }}>
                <Box>
                                     <TextField
                     fullWidth
                     label="Card Number"
                     value={formData.cardNumber}
                     onChange={handleCardNumberChange}
                     required
                     disabled={loading}
                     placeholder="1234 5678 9012 3456"
                     inputProps={{ maxLength: 19 }}
                     helperText="16 digits maximum"
                   />
                </Box>
                
                <Box sx={{ 
                  display: 'grid',
                  gridTemplateColumns: { xs: '1fr', sm: '2fr 1fr 1fr' },
                  gap: 3 
                }}>
                  <Box>
                    <TextField
                      fullWidth
                      label="Card Holder Name"
                      value={formData.cardHolderName}
                      onChange={handleInputChange('cardHolderName')}
                      required
                      disabled={loading}
                      placeholder="John Doe"
                    />
                  </Box>
                  
                  <Box>
                    <TextField
                      fullWidth
                      label="Expiry Date"
                      value={formData.expiryDate}
                      onChange={handleExpiryDateChange}
                      required
                      disabled={loading}
                      placeholder="MM/YY"
                    />
                  </Box>
                  
                  <Box>
                    <TextField
                      fullWidth
                      label="CVV"
                      value={formData.cvv}
                      onChange={handleInputChange('cvv')}
                      required
                      disabled={loading}
                      type="password"
                      inputProps={{ maxLength: 4 }}
                      placeholder="123"
                    />
                  </Box>
                </Box>
              </Box>
            </Box>

                         {/* Test Cards Section */}
             <Box sx={{ 
               mt: 4, 
               p: 3, 
               bgcolor: 'background.paper', 
               borderRadius: 2,
               border: '1px solid',
               borderColor: 'divider'
             }}>
               <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                 <CreditCard />
                 Test Cards
               </Typography>
               
               <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' }, gap: 2 }}>
                 {testCards.map((card, index) => (
                   <Card 
                     key={index}
                     sx={{ 
                       cursor: 'pointer',
                       transition: 'transform 0.2s, box-shadow 0.2s',
                       '&:hover': {
                         transform: 'translateY(-2px)',
                         boxShadow: 2,
                       }
                     }}
                     onClick={() => fillTestCard(card)}
                   >
                     <CardContent sx={{ p: 2 }}>
                       <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 1 }}>
                         {card.name}
                       </Typography>
                       <Typography variant="body2" sx={{ fontFamily: 'monospace', mb: 1 }}>
                         {card.cardNumber}
                       </Typography>
                       <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
                         {card.cardHolderName} • {card.expiryDate} • {card.cvv}
                       </Typography>
                       <Typography variant="caption" color="text.secondary">
                         {card.description}
                       </Typography>
                     </CardContent>
                   </Card>
                 ))}
               </Box>
               
               <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
                 💡 Click on any card to auto-fill the form
               </Typography>
             </Box>

             <Box sx={{ mt: 4, display: 'flex', alignItems: 'center', gap: 2 }}>
               <Security color="action" />
               <Typography variant="body2" color="text.secondary">
                 Your payment information is encrypted and secure
               </Typography>
             </Box>

            <Box sx={{ mt: 4, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
              <Button
                type="button"
                variant="outlined"
                disabled={loading}
                onClick={() => {
                  setFormData({
                    customerId: '',
                    amount: '',
                    currency: 'TRY',
                    paymentMethod: 'CREDIT_CARD',
                    cardNumber: '',
                    cardHolderName: '',
                    expiryDate: '',
                    cvv: '',
                    description: '',
                  });
                  setError(null);
                  setSuccess(null);
                }}
              >
                Clear Form
              </Button>
              
              <Button
                type="submit"
                variant="contained"
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} /> : <Payment />}
                sx={{ minWidth: 150 }}
              >
                {loading ? 'Processing...' : 'Process Payment'}
              </Button>
            </Box>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
};

export default ProcessPaymentPage;
