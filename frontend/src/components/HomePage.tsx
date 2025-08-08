import React from 'react';
import { Container, Typography, Card, CardContent, Button, Box } from '@mui/material';
import { CreditCard, Security, Speed, Support } from '@mui/icons-material';

const HomePage: React.FC = () => {
  const handlePaymentRedirect = () => {
    window.location.href = '/payment';
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box textAlign="center" mb={6}>
        <Typography variant="h2" component="h1" gutterBottom color="primary">
          Payment Gateway
        </Typography>
        <Typography variant="h5" color="text.secondary" paragraph>
          Güvenli ve hızlı ödeme çözümleri
        </Typography>
      </Box>

      <Box 
        sx={{ 
          display: 'flex', 
          flexWrap: 'wrap', 
          gap: 3, 
          mb: 6,
          justifyContent: 'center'
        }}
      >
        <Box sx={{ flex: '1 1 250px', maxWidth: 300 }}>
          <Card sx={{ height: '100%', textAlign: 'center', p: 2 }}>
            <CardContent>
              <Security color="primary" sx={{ fontSize: 48, mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                Güvenli
              </Typography>
              <Typography variant="body2" color="text.secondary">
                256-bit SSL şifreleme ile güvenli ödeme
              </Typography>
            </CardContent>
          </Card>
        </Box>
        <Box sx={{ flex: '1 1 250px', maxWidth: 300 }}>
          <Card sx={{ height: '100%', textAlign: 'center', p: 2 }}>
            <CardContent>
              <Speed color="primary" sx={{ fontSize: 48, mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                Hızlı
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Anında işlem onayı
              </Typography>
            </CardContent>
          </Card>
        </Box>
        <Box sx={{ flex: '1 1 250px', maxWidth: 300 }}>
          <Card sx={{ height: '100%', textAlign: 'center', p: 2 }}>
            <CardContent>
              <CreditCard color="primary" sx={{ fontSize: 48, mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                Çoklu Kart
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Tüm kredi ve banka kartları kabul edilir
              </Typography>
            </CardContent>
          </Card>
        </Box>
        <Box sx={{ flex: '1 1 250px', maxWidth: 300 }}>
          <Card sx={{ height: '100%', textAlign: 'center', p: 2 }}>
            <CardContent>
              <Support color="primary" sx={{ fontSize: 48, mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                7/24 Destek
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Kesintisiz müşteri desteği
              </Typography>
            </CardContent>
          </Card>
        </Box>
      </Box>

      <Box textAlign="center">
        <Button 
          variant="contained" 
          size="large" 
          onClick={handlePaymentRedirect}
          sx={{ px: 4, py: 2 }}
        >
          Ödeme Yap
        </Button>
      </Box>
    </Container>
  );
};

export default HomePage;
