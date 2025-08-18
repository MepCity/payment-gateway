import axios from 'axios';
import { PaymentListItem, PaymentDetail, PaymentStats, DashboardFilters, PaginationInfo } from '../types/dashboard';

const API_BASE_URL = 'http://localhost:8080';

// Create axios instance with interceptors
const dashboardApiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth headers
dashboardApiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  const apiKey = localStorage.getItem('auth_api_key');
  
  console.log('Dashboard API - Token:', token ? 'Present' : 'Missing');
  console.log('Dashboard API - API Key:', apiKey ? 'Present' : 'Missing');
  
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  
  if (apiKey) {
    config.headers['X-API-Key'] = apiKey;
  }
  
  return config;
});

// Response interceptor for error handling
dashboardApiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Unauthorized - redirect to login
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      localStorage.removeItem('auth_api_key');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export interface PaymentListResponse {
  payments: PaymentListItem[];
  pagination: PaginationInfo;
  stats?: PaymentStats;
}

export const dashboardAPI = {
  // Get payment statistics
  getPaymentStats: async (merchantId: string): Promise<PaymentStats> => {
    try {
      // For test mode, get all payments regardless of merchant ID
      const response = await dashboardApiClient.get(`/v1/payments`);
      const payments = response.data;
      
      // Calculate stats from payments
      const stats = {
        totalPayments: payments.length,
        successfulPayments: payments.filter((p: any) => p.status === 'COMPLETED').length,
        failedPayments: payments.filter((p: any) => p.status === 'FAILED').length,
        pendingPayments: payments.filter((p: any) => p.status === 'PENDING' || p.status === 'PROCESSING').length,
        totalAmount: payments.reduce((sum: number, p: any) => sum + p.amount, 0),
        successRate: payments.length > 0 ? (payments.filter((p: any) => p.status === 'COMPLETED').length / payments.length) * 100 : 0,
        averageAmount: payments.length > 0 ? payments.reduce((sum: number, p: any) => sum + p.amount, 0) / payments.length : 0,
      };
      
      return stats;
    } catch (error) {
      console.error('Get payment stats error:', error);
      // Return default stats on error
      return {
        totalPayments: 0,
        successfulPayments: 0,
        failedPayments: 0,
        pendingPayments: 0,
        totalAmount: 0,
        successRate: 0,
        averageAmount: 0,
      };
    }
  },

  // Get payments list with filters and pagination
  getPayments: async (
    merchantId: string, 
    filters?: DashboardFilters,
    page: number = 1,
    pageSize: number = 25
  ): Promise<PaymentListResponse> => {
    try {
      // For test mode, get all payments regardless of merchant ID
      console.log('Fetching payments from backend...');
      const response = await dashboardApiClient.get(`/v1/payments`);
      let payments = response.data;
      console.log('Backend payments response:', payments);

      // Convert date arrays to ISO strings if needed
      payments = payments.map((p: any) => ({
        ...p,
        createdAt: Array.isArray(p.createdAt) ? 
          new Date(p.createdAt[0], p.createdAt[1] - 1, p.createdAt[2], p.createdAt[3], p.createdAt[4], p.createdAt[5]).toISOString() : 
          p.createdAt,
        updatedAt: Array.isArray(p.updatedAt) ? 
          new Date(p.updatedAt[0], p.updatedAt[1] - 1, p.updatedAt[2], p.updatedAt[3], p.updatedAt[4], p.updatedAt[5]).toISOString() : 
          p.updatedAt,
        completedAt: Array.isArray(p.completedAt) ? 
          new Date(p.completedAt[0], p.completedAt[1] - 1, p.completedAt[2], p.completedAt[3], p.completedAt[4], p.completedAt[5]).toISOString() : 
          p.completedAt,
      }));

      // Apply filters
      if (filters?.search) {
        const searchTerm = filters.search.toLowerCase();
        payments = payments.filter((p: any) => 
          p.paymentId.toLowerCase().includes(searchTerm) ||
          p.transactionId.toLowerCase().includes(searchTerm) ||
          p.customerId.toLowerCase().includes(searchTerm) ||
          p.cardHolderName.toLowerCase().includes(searchTerm)
        );
      }
      
      if (filters?.status && filters.status.length > 0) {
        payments = payments.filter((p: any) => filters.status!.includes(p.status));
      }
      
      if (filters?.paymentMethod && filters.paymentMethod.length > 0) {
        payments = payments.filter((p: any) => filters.paymentMethod!.includes(p.paymentMethod));
      }
      
      if (filters?.dateRange) {
        if (filters.dateRange.startDate) {
          const startDate = new Date(filters.dateRange.startDate);
          payments = payments.filter((p: any) => new Date(p.createdAt) >= startDate);
        }
        if (filters.dateRange.endDate) {
          const endDate = new Date(filters.dateRange.endDate);
          payments = payments.filter((p: any) => new Date(p.createdAt) <= endDate);
        }
      }
      
      if (filters?.amountRange) {
        payments = payments.filter((p: any) => 
          p.amount >= filters.amountRange!.min && p.amount <= filters.amountRange!.max
        );
      }

      // Sort by creation date (newest first)
      payments.sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

      // Apply pagination
      const totalCount = payments.length;
      const startIndex = (page - 1) * pageSize;
      const paginatedPayments = payments.slice(startIndex, startIndex + pageSize);

      return {
        payments: paginatedPayments.map((p: any) => ({
          id: p.id,
          paymentId: p.paymentId || p.payment_id,
          transactionId: p.transactionId || p.transaction_id,
          merchantId: p.merchantId || p.merchant_id,
          customerId: p.customerId || p.customer_id,
          amount: p.amount,
          currency: p.currency,
          status: p.status,
          paymentMethod: p.paymentMethod || p.payment_method,
          cardNumber: p.cardNumber || p.card_number, // Already masked from backend
          cardHolderName: p.cardHolderName || p.card_holder_name,
          description: p.description || '',
          gatewayResponse: p.gatewayResponse || p.gateway_response,
          gatewayTransactionId: p.gatewayTransactionId || p.gateway_transaction_id,
          createdAt: p.createdAt || p.created_at,
          updatedAt: p.updatedAt || p.updated_at,
        })),
        pagination: {
          page,
          pageSize,
          totalCount,
          totalPages: Math.ceil(totalCount / pageSize),
          hasNext: page < Math.ceil(totalCount / pageSize),
          hasPrev: page > 1,
        }
      };
    } catch (error) {
      console.error('Get payments error:', error);
      return {
        payments: [],
        pagination: {
          page,
          pageSize,
          totalCount: 0,
          totalPages: 0,
          hasNext: false,
          hasPrev: false,
        }
      };
    }
  },

  // Get payment detail with events and logs
  getPaymentDetail: async (paymentId: string): Promise<PaymentDetail> => {
    try {
      // Try to get by payment ID first, then by transaction ID
      let response;
      try {
        response = await dashboardApiClient.get(`/v1/payments/${paymentId}`);
      } catch (error: any) {
        if (error.response?.status === 404) {
          // Try with transaction ID
          response = await dashboardApiClient.get(`/v1/payments/transaction/${paymentId}`);
        } else {
          throw error;
        }
      }
      
      const payment = response.data;
      
      // Convert backend response to frontend format
      return {
        id: payment.id,
        paymentId: payment.paymentId || payment.payment_id,
        transactionId: payment.transactionId || payment.transaction_id,
        merchantId: payment.merchantId || payment.merchant_id,
        customerId: payment.customerId || payment.customer_id,
        amount: payment.amount,
        currency: payment.currency,
        status: payment.status,
        paymentMethod: payment.paymentMethod || payment.payment_method,
        cardNumber: payment.cardNumber || payment.card_number,
        cardHolderName: payment.cardHolderName || payment.card_holder_name,
        cardBrand: payment.cardBrand || payment.card_brand,
        cardBin: payment.cardBin || payment.card_bin,
        cardLastFour: payment.cardLastFour || payment.card_last_four,
        description: payment.description || '',
        gatewayResponse: payment.gatewayResponse || payment.gateway_response,
        gatewayTransactionId: payment.gatewayTransactionId || payment.gateway_transaction_id,
        createdAt: payment.createdAt || payment.created_at,
        updatedAt: payment.updatedAt || payment.updated_at,
        completedAt: payment.completedAt || payment.completed_at,
        // Mock events and logs for now
        events: [
          {
            id: '1',
            eventType: 'PAYMENT_INITIATED',
            timestamp: payment.createdAt,
            status: 'INFO' as const,
            message: 'Payment request received',
            details: { amount: payment.amount, currency: payment.currency }
          },
          {
            id: '2',
            eventType: 'PAYMENT_PROCESSING',
            timestamp: payment.updatedAt,
            status: payment.status === 'COMPLETED' ? 'SUCCESS' as const : payment.status === 'FAILED' ? 'FAILED' as const : 'INFO' as const,
            message: `Payment ${payment.status.toLowerCase()}`,
            details: { gateway: payment.gatewayTransactionId }
          }
        ],
        logs: [
          {
            id: '1',
            level: 'INFO' as const,
            timestamp: payment.createdAt,
            message: `POST /v1/payments - Payment created`,
            source: 'API' as const,
            latency: 150,
            urlPath: '/v1/payments'
          },
          {
            id: '2',
            level: payment.status === 'FAILED' ? 'ERROR' as const : 'INFO' as const,
            timestamp: payment.updatedAt,
            message: `Payment ${payment.status} - ${payment.gatewayResponse || 'No response'}`,
            source: 'GATEWAY' as const,
            latency: 200,
            urlPath: '/gateway/process'
          }
        ]
      };
    } catch (error) {
      console.error('Get payment detail error:', error);
      throw error;
    }
  },

  // Get payment events and logs (like Hyperswitch)
  getPaymentEvents: async (paymentId: string) => {
    // This will be implemented when backend supports it
    // const response = await dashboardApiClient.get(`/v1/payments/${paymentId}/events`);
    // return response.data;
    
    // Mock data for now
    return {
      events: [
        {
          id: '1',
          eventType: 'PAYMENT_INITIATED',
          timestamp: new Date().toISOString(),
          status: 'INFO' as const,
          message: 'Payment request received',
          details: { amount: 100, currency: 'TRY' }
        },
        {
          id: '2', 
          eventType: 'PAYMENT_PROCESSING',
          timestamp: new Date().toISOString(),
          status: 'INFO' as const,
          message: 'Payment being processed',
          details: { gateway: 'Garanti BBVA' }
        }
      ],
      logs: [
        {
          id: '1',
          level: 'INFO' as const,
          timestamp: new Date().toISOString(),
          message: 'POST /v1/payments - 200 OK',
          source: 'API' as const,
          latency: 150,
          urlPath: '/v1/payments'
        }
      ]
    };
  },

  // Sync payment status (like Hyperswitch sync button)
  syncPaymentStatus: async (paymentId: string) => {
    const response = await dashboardApiClient.post(`/v1/payments/${paymentId}/sync`);
    return response.data;
  }
};
