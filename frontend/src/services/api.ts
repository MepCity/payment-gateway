import axios from 'axios';
import { PaymentRequest, PaymentResponse, PaymentStatus } from '../types/payment';

const API_BASE_URL = 'http://localhost:8080/api';

// MERCH001 için API key - gerçek uygulamada environment variable'dan gelir
const API_KEY = 'pk_merch001_live_abc123';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'X-API-Key': API_KEY,
  },
});

// Individual export functions for easier import
export const createPayment = (paymentData: PaymentRequest): Promise<PaymentResponse> =>
  api.post('/v1/payments', paymentData).then(response => response.data);

export const getPayment = (id: number): Promise<PaymentResponse> =>
  api.get(`/v1/payments/${id}`).then(response => response.data);

export const getPaymentByTransactionId = (transactionId: string): Promise<PaymentResponse> =>
  api.get(`/v1/payments/transaction/${transactionId}`).then(response => response.data);

export const getAllPayments = (): Promise<PaymentResponse[]> =>
  api.get('/v1/payments').then(response => response.data);

// Payment API methods object (alternative way to import)
export const paymentAPI = {
  createPayment,
  getPayment,
  getPaymentByTransactionId,
  getAllPayments,

  // Get payments by merchant
  getPaymentsByMerchant: (merchantId: string): Promise<PaymentResponse[]> =>
    api.get(`/v1/payments/merchant/${merchantId}`).then(response => response.data),

  // Get payments by customer
  getPaymentsByCustomer: (customerId: string): Promise<PaymentResponse[]> =>
    api.get(`/v1/payments/customer/${customerId}`).then(response => response.data),

  // Get payments by status
  getPaymentsByStatus: (status: PaymentStatus): Promise<PaymentResponse[]> =>
    api.get(`/v1/payments/status/${status}`).then(response => response.data),

  // Update payment status
  updatePaymentStatus: (id: number, status: PaymentStatus): Promise<PaymentResponse> =>
    api.put(`/v1/payments/${id}/status?status=${status}`).then(response => response.data),

  // Delete payment
  deletePayment: (id: number): Promise<void> =>
    api.delete(`/v1/payments/${id}`).then(response => response.data),

  // Refund payment
  refundPayment: (id: number): Promise<PaymentResponse> =>
    api.post(`/v1/payments/${id}/refund`).then(response => response.data),

  // Health check using Spring Boot Actuator
  healthCheck: (): Promise<any> =>
    api.get('/actuator/health').then(response => response.data),
};

export default api;
