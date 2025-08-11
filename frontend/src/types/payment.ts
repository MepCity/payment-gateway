export interface Payment {
  id?: number;
  paymentId?: string;
  transactionId?: string;
  merchantId: string;
  customerId: string;
  amount: string;
  currency: string;
  status?: PaymentStatus;
  paymentMethod: PaymentMethod;
  cardNumber: string;
  cardHolderName: string;
  expiryDate: string;
  description?: string;
  gatewayResponse?: string;
  gatewayTransactionId?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PaymentRequest {
  merchantId: string;
  customerId: string;
  amount: string;
  currency: string;
  paymentMethod: PaymentMethod;
  cardNumber: string;
  cardHolderName: string;
  expiryDate: string;
  cvv: string;
  description?: string;
}

export interface PaymentResponse {
  id?: number;
  paymentId?: string;
  transactionId?: string;
  merchantId: string;
  customerId: string;
  amount: string;
  currency: string;
  status: PaymentStatus;
  paymentMethod: PaymentMethod;
  cardNumber: string;
  cardHolderName: string;
  description?: string;
  gatewayResponse?: string;
  gatewayTransactionId?: string;
  createdAt?: string;
  updatedAt?: string;
  message?: string;
  success: boolean;
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  BANK_TRANSFER = 'BANK_TRANSFER',
  DIGITAL_WALLET = 'DIGITAL_WALLET'
}
