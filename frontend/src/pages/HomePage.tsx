import React from 'react';
import { Box, Typography, Container, Paper } from '@mui/material';
import PaymentPage from '../components/PaymentPage';

const HomePage: React.FC = () => {
  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ textAlign: 'center', mb: 4 }}>
        <Typography variant="h3" component="h1" gutterBottom>
          🏦 Payment Gateway
        </Typography>
        <Typography variant="h6" color="text.secondary">
          Güvenli ve hızlı ödeme çözümü
        </Typography>
      </Box>

      <Box sx={{ display: 'flex', gap: 3, flexDirection: { xs: 'column', md: 'row' } }}>
        <Box sx={{ flex: 2 }}>
          <Paper elevation={3} sx={{ p: 3 }}>
            <PaymentPage />
          </Paper>
        </Box>
        
        <Box sx={{ flex: 1 }}>
          <Paper elevation={2} sx={{ p: 3, mb: 2 }}>
            <Typography variant="h6" gutterBottom>
              📊 Test Kartları
            </Typography>
            
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2" color="primary">
                ✅ Başarılı İşlem
              </Typography>
              <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                4824940000000014
              </Typography>
              <Typography variant="caption" color="text.secondary">
                CVV: 314 | Exp: 12/25
              </Typography>
            </Box>

            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2" color="warning.main">
                🔐 3D Secure
              </Typography>
              <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                4824940000000030
              </Typography>
              <Typography variant="caption" color="text.secondary">
                CVV: 330 | Exp: 12/25
              </Typography>
            </Box>

            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2" color="error.main">
                ❌ Yetersiz Bakiye
              </Typography>
              <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                4824940000000022
              </Typography>
              <Typography variant="caption" color="text.secondary">
                CVV: 322 | Exp: 12/25
              </Typography>
            </Box>
          </Paper>

          <Paper elevation={2} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              🔗 API Endpoint'leri
            </Typography>
            
            <Typography variant="caption" display="block" gutterBottom>
              <strong>Backend:</strong> http://localhost:8080/api
            </Typography>
            
            <Typography variant="caption" display="block" gutterBottom>
              <strong>Payments:</strong> /v1/payments
            </Typography>
            
            <Typography variant="caption" display="block" gutterBottom>
              <strong>Webhooks:</strong> /v1/webhooks
            </Typography>
            
            <Typography variant="caption" display="block">
              <strong>Bank Webhooks:</strong> /api/v1/bank-webhooks/*
            </Typography>
          </Paper>
        </Box>
      </Box>
    </Container>
  );
};

export default HomePage;