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
  apiAuthType?: string;
  latency?: number;
  urlPath?: string;
  details?: Record<string, any>;
}

export interface DashboardFilters {
  dateRange?: {
    startDate?: string;
    endDate?: string;
  };
  status?: PaymentStatus[];
  paymentMethod?: PaymentMethod[];
  amountRange?: {
    min: number;
    max: number;
  };
  search?: string;
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

export interface PaginationInfo {
  page: number;
  pageSize: number;
  totalCount: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev: boolean;
}
