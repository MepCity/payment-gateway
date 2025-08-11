import React, { useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Box,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  CircularProgress
} from '@mui/material';
import { CreditCard, Security } from '@mui/icons-material';
import { PaymentRequest, PaymentResponse, PaymentMethod } from '../types/payment';
import { createPayment } from '../services/api';

const PaymentPage: React.FC = () => {
  const [formData, setFormData] = useState<PaymentRequest>({
    merchantId: 'MERCH001',
    customerId: '',
    amount: '',
    currency: 'TRY',
    paymentMethod: PaymentMethod.CREDIT_CARD,
    cardNumber: '',
    cardHolderName: '',
    expiryDate: '',
    cvv: '',
    description: ''
  });

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<{ success: boolean; message: string; data?: PaymentResponse } | null>(null);

  const handleInputChange = (field: keyof PaymentRequest) => (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const value = event.target.value;
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSelectChange = (field: keyof PaymentRequest) => (
    event: any
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value
    }));
  };

  const formatCardNumber = (value: string) => {
    const v = value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
    const matches = v.match(/\d{4,16}/g);
    const match = (matches && matches[0]) || '';
    const parts = [];
    for (let i = 0, len = match.length; i < len; i += 4) {
      parts.push(match.substring(i, i + 4));
    }
    if (parts.length) {
      return parts.join(' ');
    } else {
      return v;
    }
  };

  const handleCardNumberChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatCardNumber(event.target.value);
    setFormData(prev => ({
      ...prev,
      cardNumber: formatted.replace(/\s/g, '')
    }));
  };

  const formatExpiryDate = (value: string) => {
    const v = value.replace(/\D/g, '');
    if (v.length >= 2) {
      return `${v.slice(0, 2)}/${v.slice(2, 4)}`;
    }
    return v;
  };

  const handleExpiryDateChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatExpiryDate(event.target.value);
    setFormData(prev => ({
      ...prev,
      expiryDate: formatted
    }));
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setResult(null);

    try {
      const response = await createPayment(formData);
      setResult({
        success: true,
        message: 'Ödeme başarıyla oluşturuldu!',
        data: response
      });
    } catch (error: any) {
      console.error('Payment error details:', error);
      
      let errorMessage = 'Ödeme işlemi sırasında bir hata oluştu.';
      
      if (error.response) {
        // Backend'den gelen hata
        console.log('Error response:', error.response);
        console.log('Error status:', error.response.status);
        console.log('Error data:', error.response.data);
        
        if (error.response.data && error.response.data.errors) {
          // Validation hataları
          const validationErrors = error.response.data.errors;
          errorMessage = 'Validation hataları:\n' + Object.entries(validationErrors)
            .map(([field, message]) => `${field}: ${message}`)
            .join('\n');
        } else if (error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message;
        }
      } else if (error.request) {
        // Network hatası
        errorMessage = 'Sunucuya bağlanılamadı. Lütfen internet bağlantınızı kontrol edin.';
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      setResult({
        success: false,
        message: errorMessage
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Box textAlign="center" mb={4}>
        <Typography variant="h3" component="h1" gutterBottom color="primary">
          <CreditCard sx={{ mr: 2, verticalAlign: 'middle' }} />
          Ödeme Sayfası
        </Typography>
      </Box>

      <Paper elevation={3} sx={{ p: 4 }}>
        <form onSubmit={handleSubmit}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
            {/* Müşteri Bilgileri */}
            <Typography variant="h6" gutterBottom>
              Müşteri Bilgileri
            </Typography>
            
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Box sx={{ flex: '1 1 300px' }}>
                <TextField
                  fullWidth
                  label="Müşteri ID"
                  value={formData.customerId}
                  onChange={handleInputChange('customerId')}
                  required
                />
              </Box>
              <Box sx={{ flex: '1 1 300px' }}>
                <TextField
                  fullWidth
                  label="Açıklama"
                  value={formData.description}
                  onChange={handleInputChange('description')}
                />
              </Box>
            </Box>

            {/* Ödeme Bilgileri */}
            <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
              Ödeme Bilgileri
            </Typography>
            
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Box sx={{ flex: '1 1 300px' }}>
                              <TextField
                fullWidth
                label="Tutar"
                type="text"
                value={formData.amount}
                onChange={handleInputChange('amount')}
                placeholder="0.00"
                required
              />
              </Box>
              <Box sx={{ flex: '1 1 300px' }}>
                <FormControl fullWidth>
                  <InputLabel>Para Birimi</InputLabel>
                  <Select
                    value={formData.currency}
                    label="Para Birimi"
                    onChange={handleSelectChange('currency')}
                  >
                    <MenuItem value="TRY">TRY - Türk Lirası</MenuItem>
                    <MenuItem value="USD">USD - Amerikan Doları</MenuItem>
                    <MenuItem value="EUR">EUR - Euro</MenuItem>
                  </Select>
                </FormControl>
              </Box>
            </Box>

            {/* Kart Bilgileri */}
            <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
              Kart Bilgileri
            </Typography>
            
            <TextField
              fullWidth
              label="Kart Numarası"
              value={formatCardNumber(formData.cardNumber)}
              onChange={handleCardNumberChange}
              placeholder="1234 5678 9012 3456"
              inputProps={{ maxLength: 19 }}
              required
            />
            
            <TextField
              fullWidth
              label="Kart Sahibi Adı"
              value={formData.cardHolderName}
              onChange={handleInputChange('cardHolderName')}
              required
            />
            
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Box sx={{ flex: '1 1 300px' }}>
                <TextField
                  fullWidth
                  label="Son Kullanma Tarihi"
                  value={formData.expiryDate}
                  onChange={handleExpiryDateChange}
                  placeholder="MM/YY"
                  inputProps={{ maxLength: 5 }}
                  required
                />
              </Box>
              <Box sx={{ flex: '1 1 200px' }}>
                <TextField
                  fullWidth
                  label="CVV"
                  value={formData.cvv}
                  onChange={handleInputChange('cvv')}
                  placeholder="123"
                  inputProps={{ maxLength: 4, type: 'password' }}
                  required
                />
              </Box>
            </Box>

            {/* Güvenlik Bilgisi */}
            <Card sx={{ bgcolor: 'primary.light', color: 'primary.contrastText' }}>
              <CardContent>
                <Box display="flex" alignItems="center">
                  <Security sx={{ mr: 1 }} />
                  <Typography variant="body2">
                    Tüm kart bilgileriniz 256-bit SSL şifreleme ile korunmaktadır.
                  </Typography>
                </Box>
              </CardContent>
            </Card>

            {/* Sonuç Mesajı */}
            {result && (
              <Alert severity={result.success ? 'success' : 'error'}>
                {result.message}
                {result.success && result.data && (
                  <Box sx={{ mt: 1 }}>
                    <Typography variant="body2">
                      Ödeme ID: {result.data.paymentId}
                    </Typography>
                    <Typography variant="body2">
                      İşlem ID: {result.data.transactionId}
                    </Typography>
                    <Typography variant="body2">
                      Durum: {result.data.status}
                    </Typography>
                  </Box>
                )}
              </Alert>
            )}

            {/* Submit Button */}
            <Button
              type="submit"
              variant="contained"
              size="large"
              fullWidth
              disabled={loading}
              sx={{ py: 2 }}
            >
              {loading ? (
                <>
                  <CircularProgress size={20} sx={{ mr: 1 }} />
                  İşleniyor...
                </>
              ) : (
                'Ödemeyi Tamamla'
              )}
            </Button>
          </Box>
        </form>
      </Paper>
    </Container>
  );
};

export default PaymentPage;
