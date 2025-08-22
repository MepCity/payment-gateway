import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Paper,
  Button,
  Chip,
  Divider,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio
} from '@mui/material';
import {
  ArrowBack,
  AttachFile,
  Edit
} from '@mui/icons-material';
import { format } from 'date-fns';
import dashboardAPI from '../services/dashboardApi';
import {
  DisputeDetail,
  DisputeStatus,
  DisputeReason,
  DisputeResponseType
} from '../types/dashboard';

const DisputeDetailPage: React.FC = () => {
  const { disputeId } = useParams<{ disputeId: string }>();
  const navigate = useNavigate();
  
  // States
  const [dispute, setDispute] = useState<DisputeDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [responseDialogOpen, setResponseDialogOpen] = useState(false);
  const [responseType, setResponseType] = useState<DisputeResponseType>(DisputeResponseType.DEFEND);
  const [evidence, setEvidence] = useState('');
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Load dispute detail
  const loadDisputeDetail = async () => {
    if (!disputeId) return;
    
    try {
      setLoading(true);
      setError(null);
      console.log('🔍 Loading dispute detail for ID:', disputeId);
      const disputeDetail = await dashboardAPI.getDisputeDetail('MERCH001', disputeId);
      console.log('✅ Dispute detail loaded:', disputeDetail);
      setDispute(disputeDetail);
    } catch (err: any) {
      console.error('❌ Error loading dispute detail:', err);
      setError(`Dispute detayları yüklenirken hata oluştu: ${err.response?.data?.message || err.message}`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDisputeDetail();
  }, [disputeId]);

  // Helper functions
  const parseBackendDate = (dateData: any): Date => {
    try {
      if (typeof dateData === 'string') {
        const parsedDate = new Date(dateData);
        if (!isNaN(parsedDate.getTime())) {
          return parsedDate;
        }
      }
      
      if (!dateData) {
        return new Date();
      }
      
      if (Array.isArray(dateData) && dateData.length >= 3) {
        const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = dateData;
        
        if (typeof year === 'number' && typeof month === 'number' && typeof day === 'number') {
          const date = new Date(year, month - 1, day, hour || 0, minute || 0, second || 0, Math.floor((nano || 0) / 1000000));
          if (!isNaN(date.getTime())) {
            return date;
          }
        }
      }
    } catch (error) {
      console.error('Date parsing error:', dateData, error);
    }
    
    return new Date();
  };

  const getStatusColor = (status: DisputeStatus): 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' => {
    switch (status) {
      case DisputeStatus.WON:
        return 'success';
      case DisputeStatus.LOST:
        return 'error';
      case DisputeStatus.UNDER_REVIEW:
        return 'warning';
      case DisputeStatus.EVIDENCE_REQUIRED:
        return 'info';
      case DisputeStatus.OPENED:
        return 'primary';
      case DisputeStatus.RESOLVED:
        return 'success';
      case DisputeStatus.CLOSED:
        return 'secondary';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: DisputeStatus): string => {
    switch (status) {
      case DisputeStatus.OPENED:
        return 'Açıldı';
      case DisputeStatus.UNDER_REVIEW:
        return 'İnceleme Altında';
      case DisputeStatus.EVIDENCE_REQUIRED:
        return 'Kanıt Gerekli';
      case DisputeStatus.RESOLVED:
        return 'Çözüldü';
      case DisputeStatus.CLOSED:
        return 'Kapatıldı';
      case DisputeStatus.WON:
        return 'Kazanıldı';
      case DisputeStatus.LOST:
        return 'Kaybedildi';
      default:
        return status;
    }
  };

  const getReasonText = (reason: DisputeReason): string => {
    switch (reason) {
      case DisputeReason.FRAUD:
        return 'Dolandırıcılık';
      case DisputeReason.DUPLICATE:
        return 'Çift İşlem';
      case DisputeReason.PRODUCT_NOT_RECEIVED:
        return 'Ürün Teslim Alınmadı';
      case DisputeReason.PRODUCT_NOT_AS_DESCRIBED:
        return 'Ürün Açıklaması Uymuyor';
      case DisputeReason.CREDIT_NOT_PROCESSED:
        return 'İade İşlenmedi';
      case DisputeReason.GENERAL:
        return 'Genel';
      case DisputeReason.OTHER:
        return 'Diğer';
      default:
        return reason;
    }
  };

  const handleResponse = async () => {
    if (!disputeId) return;
    
    try {
      setSubmitting(true);
      await dashboardAPI.respondToDispute('MERCH001', disputeId, {
        responseType,
        evidence,
        notes
      });
      
      setResponseDialogOpen(false);
      setEvidence('');
      setNotes('');
      
      // Reload dispute detail
      loadDisputeDetail();
      
    } catch (err) {
      console.error('Error submitting response:', err);
      setError('Cevap gönderilirken hata oluştu');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (error || !dispute) {
    return (
      <Box p={3}>
        <Alert severity="error">{error || 'Dispute bulunamadı'}</Alert>
        <Button
          variant="contained"
          startIcon={<ArrowBack />}
          onClick={() => navigate('/dashboard/disputes')}
          sx={{ mt: 2 }}
        >
          Disputes Listesine Dön
        </Button>
      </Box>
    );
  }

  return (
    <Box p={3}>
      {/* Header */}
      <Box display="flex" alignItems="center" justifyContent="space-between" mb={3}>
        <Box display="flex" alignItems="center" gap={2}>
          <Button
            variant="outlined"
            startIcon={<ArrowBack />}
            onClick={() => navigate('/dashboard/disputes')}
          >
            Geri
          </Button>
          <Typography variant="h4" fontWeight="bold">
            Dispute Detayı
          </Typography>
          <Chip
            size="small"
            color={getStatusColor(dispute.status)}
            label={getStatusText(dispute.status)}
          />
        </Box>
        
        {(dispute.status === DisputeStatus.OPENED || dispute.status === DisputeStatus.EVIDENCE_REQUIRED) && (
          <Button
            variant="contained"
            startIcon={<Edit />}
            onClick={() => setResponseDialogOpen(true)}
          >
            Cevap Ver
          </Button>
        )}
      </Box>

      <Box display="flex" flexDirection="column" gap={3}>
        {/* First Row */}
        <Box display="flex" gap={3} flexWrap="wrap">
          {/* Basic Info */}
          <Box flex={1} minWidth="300px">
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Temel Bilgiler
                </Typography>
                <Divider sx={{ mb: 2 }} />
                
                <Box display="grid" gap={1}>
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">Dispute ID:</Typography>
                    <Typography variant="body2" fontFamily="monospace">{dispute.disputeId}</Typography>
                  </Box>
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">Payment ID:</Typography>
                    <Typography variant="body2" fontFamily="monospace">{dispute.paymentId}</Typography>
                  </Box>
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">Customer ID:</Typography>
                    <Typography variant="body2" fontFamily="monospace">{dispute.customerId}</Typography>
                  </Box>
                  {dispute.transactionId && (
                    <Box display="flex" justifyContent="space-between">
                      <Typography variant="body2" color="text.secondary">Transaction ID:</Typography>
                      <Typography variant="body2" fontFamily="monospace">{dispute.transactionId}</Typography>
                    </Box>
                  )}
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">Merchant ID:</Typography>
                    <Typography variant="body2" fontFamily="monospace">{dispute.merchantId}</Typography>
                  </Box>
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">Tutar:</Typography>
                    <Typography variant="body2" fontWeight="medium">
                      {dispute.currency} {dispute.amount.toLocaleString()}
                    </Typography>
                  </Box>
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">Sebep:</Typography>
                    <Typography variant="body2">{getReasonText(dispute.reason)}</Typography>
                  </Box>
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">Dispute Tarihi:</Typography>
                    <Typography variant="body2">
                      {(() => {
                        try {
                          if (!dispute.disputeDate) return 'Belirtilmemiş';
                          const parsedDate = parseBackendDate(dispute.disputeDate);
                          return format(parsedDate, 'dd.MM.yyyy HH:mm');
                        } catch (error) {
                          return 'Geçersiz tarih';
                        }
                      })()}
                    </Typography>
                  </Box>
                  {dispute.merchantResponseDeadline && (
                    <Box display="flex" justifyContent="space-between">
                      <Typography variant="body2" color="text.secondary">Cevap Deadline:</Typography>
                      <Typography variant="body2" color="warning.main">
                        {(() => {
                          try {
                            const parsedDate = parseBackendDate(dispute.merchantResponseDeadline);
                            return format(parsedDate, 'dd.MM.yyyy HH:mm');
                          } catch (error) {
                            return 'Geçersiz tarih';
                          }
                        })()}
                      </Typography>
                    </Box>
                  )}
                </Box>
              </CardContent>
            </Card>
          </Box>

          {/* Description */}
          <Box flex={1} minWidth="300px">
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Açıklama
                </Typography>
                <Divider sx={{ mb: 2 }} />
                
                <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                  {dispute.description || 'Açıklama mevcut değil'}
                </Typography>
                
                {dispute.evidence && (
                  <Box mt={2}>
                    <Typography variant="subtitle2" gutterBottom>
                      Kanıt:
                    </Typography>
                    <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                      {dispute.evidence}
                    </Typography>
                  </Box>
                )}
              </CardContent>
            </Card>
          </Box>
        </Box>

        {/* Customer Info */}
        {dispute.customerEmail && (
          <Box>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Müşteri Bilgileri
                </Typography>
                <Divider sx={{ mb: 2 }} />
                
                <Box display="flex" justifyContent="space-between">
                  <Typography variant="body2" color="text.secondary">Email:</Typography>
                  <Typography variant="body2">{dispute.customerEmail}</Typography>
                </Box>
              </CardContent>
            </Card>
          </Box>
        )}

        {/* Notes */}
        {(dispute.merchantNotes || dispute.adminNotes) && (
          <Box>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Notlar
                </Typography>
                <Divider sx={{ mb: 2 }} />
                
                {dispute.merchantNotes && (
                  <Box mb={2}>
                    <Typography variant="subtitle2" color="primary" gutterBottom>
                      Merchant Notları:
                    </Typography>
                    <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                      {dispute.merchantNotes}
                    </Typography>
                  </Box>
                )}
                
                {dispute.adminNotes && (
                  <Box>
                    <Typography variant="subtitle2" color="secondary" gutterBottom>
                      Admin Notları:
                    </Typography>
                    <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                      {dispute.adminNotes}
                    </Typography>
                  </Box>
                )}
              </CardContent>
            </Card>
          </Box>
        )}
      </Box>

      {/* Response Dialog */}
      <Dialog open={responseDialogOpen} onClose={() => setResponseDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Dispute Cevabı</DialogTitle>
        <DialogContent>
          <Box display="grid" gap={3} pt={1}>
            <FormControl component="fieldset">
              <FormLabel component="legend">Cevap Türü</FormLabel>
              <RadioGroup
                value={responseType}
                onChange={(e) => setResponseType(e.target.value as DisputeResponseType)}
              >
                <FormControlLabel
                  value={DisputeResponseType.ACCEPT}
                  control={<Radio />}
                  label="Kabul Et (Otomatik iade işlemi başlatılır)"
                />
                <FormControlLabel
                  value={DisputeResponseType.DEFEND}
                  control={<Radio />}
                  label="Savun (Kanıt ve açıklama gönder)"
                />
              </RadioGroup>
            </FormControl>

            {responseType === DisputeResponseType.DEFEND && (
              <>
                <TextField
                  label="Kanıt/Delil"
                  multiline
                  rows={4}
                  value={evidence}
                  onChange={(e) => setEvidence(e.target.value)}
                  placeholder="Dispute'ı destekleyen kanıtlarınızı buraya yazın..."
                />
                
                <TextField
                  label="Ek Notlar"
                  multiline
                  rows={3}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Ek açıklamalar ve notlar..."
                />
              </>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setResponseDialogOpen(false)}>
            İptal
          </Button>
          <Button
            variant="contained"
            onClick={handleResponse}
            disabled={submitting || (responseType === DisputeResponseType.DEFEND && !evidence.trim())}
          >
            {submitting ? 'Gönderiliyor...' : 'Cevabı Gönder'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DisputeDetailPage;
