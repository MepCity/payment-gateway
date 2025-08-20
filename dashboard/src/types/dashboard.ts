import React from 'react';

export interface PaymentStats {
  totalPayments: number;
  successfulPayments: number;
  failedPayments: number;
  pendingPayments: number;
  totalAmount: number;
  successRate: number;
  averageAmount: number;
}

// Refund related types
export interface RefundStats {
  totalRefunds: number;
  completedRefunds: number;
  pendingRefunds: number;
  failedRefunds: number;
  totalRefundAmount: number;
  refundRate: number;
  averageRefundAmount: number;
}

export interface RefundListItem {
  id: number;
  refundId: string;
  paymentId: string;
  transactionId: string;
  merchantId: string;
  customerId: string;
  amount: number;
  currency: string;
  status: RefundStatus;
  reason: RefundReason;
  description?: string;
  gatewayResponse?: string;
  gatewayRefundId?: string;
  refundDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface RefundDetail extends RefundListItem {
  events?: RefundEvent[];
  logs?: RefundLog[];
}

export interface RefundEvent {
  id: string;
  eventType: string;
  timestamp: string;
  status: 'SUCCESS' | 'FAILED' | 'INFO';
  message: string;
  details?: Record<string, any>;
}

export interface RefundLog {
  id: string;
  level: 'INFO' | 'WARN' | 'ERROR';
  timestamp: string;
  message: string;
  source: 'API' | 'GATEWAY' | 'SDK';
  latency?: number;
  urlPath?: string;
  requestId?: string;
  merchantId?: string;
  refundId?: string;
  apiAuthType?: string;
  details?: Record<string, any>;
}

export enum RefundStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
}

export enum RefundReason {
  CUSTOMER_REQUEST = 'CUSTOMER_REQUEST',
  MERCHANT_REQUEST = 'MERCHANT_REQUEST',
  DUPLICATE_PAYMENT = 'DUPLICATE_PAYMENT',
  FRAUD = 'FRAUD',
  TECHNICAL_ERROR = 'TECHNICAL_ERROR',
  OTHER = 'OTHER',
}

export interface DashboardFilters {
  search?: string;
  status?: PaymentStatus[];
  dateFrom?: string;
  dateTo?: string;
  paymentMethod?: PaymentMethod[];
  currency?: string;
  dateRange?: {
    startDate?: string;
    endDate?: string;
  };
  amountRange?: {
    min: number;
    max: number;
  };
}

export interface RefundFilters {
  search?: string;
  status?: RefundStatus[];
  dateFrom?: string;
  dateTo?: string;
  reason?: RefundReason[];
  currency?: string;
  dateRange?: {
    startDate?: string;
    endDate?: string;
  };
  amountRange?: {
    min: number;
    max: number;
  };
}

export interface PaginationInfo {
  page: number;
  currentPage?: number;
  totalPages: number;
  totalCount: number;
  pageSize: number;
  hasNext: boolean;
  hasPrev: boolean;
}

export interface PaymentListItem {
  id: number;
  paymentId: string;
  transactionId: string;
  merchantId: string;
  customerId: string;
  amount: number;
  currency: string;
  status: PaymentStatus;
  paymentMethod: PaymentMethod;
  cardNumber: string; // masked
  cardHolderName: string;
  description?: string;
  gatewayResponse?: string;
  gatewayTransactionId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentDetail extends PaymentListItem {
  cardBrand?: string;
  cardBin?: string;
  cardLastFour?: string;
  completedAt?: string;
  events?: PaymentEvent[];
  logs?: PaymentLog[];
}

export interface PaymentEvent {
  id: string;
  eventType: string;
  timestamp: string;
  status: 'SUCCESS' | 'FAILED' | 'INFO';
  message: string;
  details?: Record<string, any>;
}

export interface PaymentLog {
  id: string;
  level: 'INFO' | 'WARN' | 'ERROR';
  timestamp: string;
  message: string;
  source: 'API' | 'SDK' | 'GATEWAY';
  requestId?: string;
  merchantId?: string;
  paymentId?: string;
  refundId?: string;
  apiAuthType?: string;
  latency?: number;
  urlPath?: string;
  details?: Record<string, any>;
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

export interface TableColumn<T> {
  key: keyof T;
  title: string;
  width?: string;
  sortable?: boolean;
  render?: (value: any, record: T) => React.ReactNode;
}
