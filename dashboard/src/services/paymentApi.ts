// Real backend payment API integration

const API_BASE_URL = 'http://localhost:8080'; // Backend server URL

export interface PaymentRequest {
  merchantId: string;
  customerId: string;
  amount: number;
  currency: string;
  paymentMethod: 'CREDIT_CARD' | 'DEBIT_CARD';
  cardNumber: string;
  cardHolderName: string;
  expiryDate: string;
  cvv: string;
  description?: string;
}

export interface PaymentResponse {
  id: number;
  paymentId: string;
  transactionId: string;
  merchantId: string;
  customerId: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  paymentMethod: 'CREDIT_CARD' | 'DEBIT_CARD';
  cardNumber: string; // masked
  cardHolderName: string;
  cardBrand?: string;
  cardBin?: string;
  cardLastFour?: string;
  description?: string;
  gatewayResponse?: string;
  gatewayTransactionId?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  message: string;
  success: boolean;
}

export interface PaymentListResponse {
  payments: PaymentResponse[];
  total: number;
  page: number;
  size: number;
}

class PaymentApiService {
  private getAuthHeaders(): Record<string, string> {
    const token = localStorage.getItem('auth_token');
    const apiKey = localStorage.getItem('auth_api_key'); // Correct key name
    
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    if (apiKey) {
      headers['X-API-Key'] = apiKey;
    }
    
    console.log('API Headers:', headers); // Debug log
    
    return headers;
  }

  async createPayment(paymentData: PaymentRequest): Promise<PaymentResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/v1/payments`, {
        method: 'POST',
        headers: this.getAuthHeaders(),
        body: JSON.stringify(paymentData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Create payment error:', error);
      throw error;
    }
  }

  async getPaymentById(id: number): Promise<PaymentResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/v1/payments/${id}`, {
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Get payment error:', error);
      throw error;
    }
  }

  async getPaymentByTransactionId(transactionId: string): Promise<PaymentResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/v1/payments/transaction/${transactionId}`, {
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Get payment by transaction ID error:', error);
      throw error;
    }
  }

  async getAllPayments(): Promise<PaymentResponse[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/v1/payments`, {
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Get all payments error:', error);
      throw error;
    }
  }

  async getPaymentsByMerchantId(merchantId: string): Promise<PaymentResponse[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/v1/payments/merchant/${merchantId}`, {
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Get payments by merchant error:', error);
      throw error;
    }
  }

  async getPaymentsByStatus(status: string): Promise<PaymentResponse[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/v1/payments/status/${status}`, {
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Get payments by status error:', error);
      throw error;
    }
  }

  async updatePaymentStatus(id: number, status: string): Promise<PaymentResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/v1/payments/${id}/status?status=${status}`, {
        method: 'PUT',
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Update payment status error:', error);
      throw error;
    }
  }

  async updatePaymentStatusByTransactionId(transactionId: string, status: string): Promise<PaymentResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/v1/payments/transaction/${transactionId}/status?status=${status}`, {
        method: 'PUT',
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Update payment status by transaction ID error:', error);
      throw error;
    }
  }

  async refundPayment(id: number): Promise<PaymentResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/v1/payments/${id}/refund`, {
        method: 'POST',
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Refund payment error:', error);
      throw error;
    }
  }

  async deletePayment(id: number): Promise<PaymentResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/v1/payments/${id}`, {
        method: 'DELETE',
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Delete payment error:', error);
      throw error;
    }
  }
}

export const paymentApi = new PaymentApiService();