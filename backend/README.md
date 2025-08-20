# Payment Gateway API

A Spring Boot-based payment gateway system that provides comprehensive payment processing capabilities with RESTful APIs.

## Features

- **Payment Processing**: Create, retrieve, update, and delete payments
- **Multiple Payment Methods**: Support for credit cards, debit cards, bank transfers, and digital wallets
- **Transaction Management**: Complete transaction lifecycle management
- **Status Tracking**: Real-time payment status updates
- **Refund Support**: Process refunds for completed payments
- **Security**: Card number masking and CVV protection
- **Validation**: Comprehensive input validation
- **Error Handling**: Global exception handling with detailed error messages

## Technology Stack

- **Spring Boot 4.0.0-M1**
- **Spring Data JPA**
- **PostgreSQL Database**
- **Lombok**
- **Spring Validation**
- **Maven**

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+

## Setup Instructions

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE payment_gateway;
```

### 2. Configuration

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payment_gateway
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## API Documentation

### Base URLs
```
Payments: http://localhost:8080/api/v1/payments
Bank Webhooks: http://localhost:8080/api/v1/bank-webhooks
Refunds: http://localhost:8080/v1/refunds
Disputes: http://localhost:8080/v1/disputes
Payouts: http://localhost:8080/v1/payouts
System Webhooks: http://localhost:8080/api/v1/webhooks
Merchants: http://localhost:8080/api/v1/merchants
```

### Payment Endpoints

#### 1. Create Payment (POST)
```http
POST /api/v1/payments
Content-Type: application/json

{
  "merchantId": "MERCH001",
  "customerId": "CUST001",
  "amount": 99.99,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/25",
  "cvv": "123",
  "description": "Online purchase"
}
```

**Response:**
```json
{
  "id": 1,
  "transactionId": "TXN-A1B2C3D4",
  "merchantId": "MERCH001",
  "customerId": "CUST001",
  "amount": 99.99,
  "currency": "USD",
  "status": "COMPLETED",
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "************1111",
  "cardHolderName": "John Doe",
  "description": "Online purchase",
  "gatewayResponse": "Payment processed successfully",
  "gatewayTransactionId": "GTW-X1Y2Z3W4",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "message": "Payment processed successfully",
  "success": true
}
```

#### 2. Get Payment by ID (GET)
```http
GET /api/v1/payments/{id}
```

#### 3. Get Payment by Transaction ID (GET)
```http
GET /api/v1/payments/transaction/{transactionId}
```

#### 4. Get All Payments (GET)
```http
GET /api/v1/payments
```

#### 5. Get Payments by Merchant ID (GET)
```http
GET /api/v1/payments/merchant/{merchantId}
```

#### 6. Get Payments by Customer ID (GET)
```http
GET /api/v1/payments/customer/{customerId}
```

#### 7. Get Payments by Status (GET)
```http
GET /api/v1/payments/status/{status}
```

**Available Statuses:**
- `PENDING`
- `PROCESSING`
- `COMPLETED`
- `FAILED`
- `CANCELLED`
- `REFUNDED`

#### 8. Update Payment Status (PUT)
```http
PUT /api/v1/payments/{id}/status?status=COMPLETED
```

#### 9. Delete Payment (DELETE)
```http
DELETE /api/v1/payments/{id}
```



#### 11. Bank Webhooks
```http
POST /api/v1/bank-webhooks/garanti
POST /api/v1/bank-webhooks/isbank
POST /api/v1/bank-webhooks/akbank
```



#### 10. Health Check (GET)
```http
GET /actuator/health
```

### Bank Webhook Endpoints

#### 1. Garanti BBVA Webhook (POST)
```http
POST /api/v1/bank-webhooks/garanti
Content-Type: application/json

{
  "eventType": "3D_SECURE_RESULT",
  "orderId": "ORD-123456",
  "status": "SUCCESS",
  "authCode": "AUTH123"
}
```

#### 2. İş Bankası Webhook (POST)
```http
POST /api/v1/bank-webhooks/isbank
Content-Type: application/json

{
  "eventType": "PAYMENT_STATUS_CHANGE",
  "orderId": "ORD-123456",
  "status": "COMPLETED"
}
```

#### 3. Akbank Webhook (POST)
```http
POST /api/v1/bank-webhooks/akbank
Content-Type: application/json

{
  "eventType": "SETTLEMENT",
  "orderId": "ORD-123456",
  "settledAmount": "99.99",
  "settlementDate": "2024-01-15"
}
```

### Customer Endpoints

#### 1. Create Customer (POST)
```http
POST /api/v1/customers
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "address": "123 Main Street",
  "notes": "VIP customer"
}
```

#### 2. Get Customer by ID (GET)
```http
GET /api/v1/customers/{id}
```

#### 3. Get Customer by Customer ID (GET)
```http
GET /api/v1/customers/customer-id/{customerId}
```

#### 4. Get Customer by Email (GET)
```http
GET /api/v1/customers/email/{email}
```

#### 5. Get All Customers (GET)
```http
GET /api/v1/customers
```

#### 6. Get Customers by Status (GET)
```http
GET /api/v1/customers/status/{status}
```



#### 9. Search Customers by Name (GET)
```http
GET /api/v1/customers/search?name=John
```

#### 10. Update Customer (PUT)
```http
PUT /api/v1/customers/{id}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "phoneNumber": "+1234567890",
  "address": "456 Oak Avenue",
  "notes": "Updated customer info"
}
```

#### 11. Update Customer Status (PUT)
```http
PUT /api/v1/customers/{id}/status?status=ACTIVE
```

#### 12. Delete Customer (DEL)
```http
DELETE /api/v1/customers/{id}
```

### Mandate Endpoints

#### 1. Create Mandate (POST)
```http
POST /api/v1/mandates
Content-Type: application/json

{
  "customerId": "CUST-12345678",
  "merchantId": "MERCH001",
  "amount": 99.99,
  "currency": "USD",
  "type": "RECURRING",
  "bankAccountNumber": "12345678",
  "bankSortCode": "123456",
  "accountHolderName": "John Doe",
  "description": "Monthly subscription",
  "startDate": "2024-01-15T10:00:00",
  "endDate": "2024-12-31T23:59:59",
  "frequency": 30,
  "maxPayments": 12
}
```

#### 2. Get Mandate by ID (GET)
```http
GET /api/v1/mandates/{id}
```

#### 3. Get Mandate by Mandate ID (GET)
```http
GET /api/v1/mandates/mandate-id/{mandateId}
```

#### 4. Get All Mandates (GET)
```http
GET /api/v1/mandates
```

#### 5. Get Mandates by Customer ID (GET)
```http
GET /api/v1/mandates/customer/{customerId}
```

#### 6. Get Mandates by Merchant ID (GET)
```http
GET /api/v1/mandates/merchant/{merchantId}
```

#### 7. Get Mandates by Status (GET)
```http
GET /api/v1/mandates/status/{status}
```

#### 8. Get Active Customer Mandates (GET)
```http
GET /api/v1/mandates/customer/{customerId}/active
```

#### 9. Revoke Mandate (POST)
```http
POST /api/v1/mandates/{id}/revoke
```

#### 10. Update Mandate Status (PUT)
```http
PUT /api/v1/mandates/{id}/status?status=ACTIVE
```

### Refund Endpoints

#### 1. Create Refund (POST)
```http
POST /api/v1/refunds
Content-Type: application/json

{
  "paymentId": "1",
  "transactionId": "TXN-12345678",
  "merchantId": "MERCH001",
  "customerId": "CUST-12345678",
  "amount": 99.99,
  "currency": "USD",
  "reason": "CUSTOMER_REQUEST",
  "description": "Customer requested refund"
}
```

#### 2. Get Refund by ID (GET)
```http
GET /api/v1/refunds/{id}
```

#### 3. Get Refund by Refund ID (GET)
```http
GET /api/v1/refunds/refund-id/{refundId}
```

#### 4. Get Refund by Payment ID (GET)
```http
GET /api/v1/refunds/payment/{paymentId}
```

#### 5. Get All Refunds (GET)
```http
GET /api/v1/refunds
```

#### 6. Get Refunds by Merchant ID (GET)
```http
GET /api/v1/refunds/merchant/{merchantId}
```

#### 7. Get Refunds by Customer ID (GET)
```http
GET /api/v1/refunds/customer/{customerId}
```

#### 8. Get Refunds by Status (GET)
```http
GET /api/v1/refunds/status/{status}
```

#### 9. Get Refunds by Reason (GET)
```http
GET /api/v1/refunds/reason/{reason}
```

#### 10. Get Refunds by Transaction ID (GET)
```http
GET /api/v1/refunds/transaction/{transactionId}
```

#### 11. Update Refund (POST)
```http
POST /api/v1/refunds/{id}/update
Content-Type: application/json

{
  "paymentId": "1",
  "transactionId": "TXN-12345678",
  "merchantId": "MERCH001",
  "customerId": "CUST-12345678",
  "amount": 50.00,
  "currency": "USD",
  "reason": "MERCHANT_REQUEST",
  "description": "Partial refund"
}
```

#### 12. Update Refund Status (PUT)
```http
PUT /api/v1/refunds/{id}/status?status=COMPLETED
```

### Dispute Endpoints

#### 1. Create Dispute (POST)
```http
POST /api/v1/disputes
Content-Type: application/json

{
  "paymentId": "1",
  "transactionId": "TXN-12345678",
  "merchantId": "MERCH001",
  "customerId": "CUST-12345678",
  "amount": 99.99,
  "currency": "USD",
  "reason": "PRODUCT_NOT_RECEIVED",
  "description": "Customer did not receive the product",
  "evidence": "Tracking number shows delivery failed"
}
```

#### 2. Get Dispute by ID (GET)
```http
GET /api/v1/disputes/{id}
```

#### 3. Get Dispute by Dispute ID (GET)
```http
GET /api/v1/disputes/dispute-id/{disputeId}
```

#### 4. Get Dispute by Payment ID (GET)
```http
GET /api/v1/disputes/payment/{paymentId}
```

#### 5. Get All Disputes (GET)
```http
GET /api/v1/disputes
```

#### 6. Get Disputes by Merchant ID (GET)
```http
GET /api/v1/disputes/merchant/{merchantId}
```

#### 7. Get Disputes by Customer ID (GET)
```http
GET /api/v1/disputes/customer/{customerId}
```

#### 8. Get Disputes by Status (GET)
```http
GET /api/v1/disputes/status/{status}
```

#### 9. Get Disputes by Reason (GET)
```http
GET /api/v1/disputes/reason/{reason}
```

#### 10. Get Disputes by Transaction ID (GET)
```http
GET /api/v1/disputes/transaction/{transactionId}
```

#### 11. Update Dispute (POST)
```http
POST /api/v1/disputes/{id}/update
Content-Type: application/json

{
  "paymentId": "1",
  "transactionId": "TXN-12345678",
  "merchantId": "MERCH001",
  "customerId": "CUST-12345678",
  "amount": 99.99,
  "currency": "USD",
  "reason": "FRAUD",
  "description": "Suspicious transaction",
  "evidence": "Multiple failed attempts"
}
```

#### 12. Update Dispute Status (PUT)
```http
PUT /api/v1/disputes/{id}/status?status=UNDER_REVIEW
```

#### 13. Close Dispute (POST)
```http
POST /api/v1/disputes/{id}/close
```

## Payment Status Flow

1. **PENDING** → Initial state when payment is created
2. **PROCESSING** → Payment is being processed by gateway
3. **COMPLETED** → Payment successfully processed
4. **FAILED** → Payment processing failed
5. **CANCELLED** → Payment was cancelled
6. **REFUNDED** → Payment was refunded

## Validation Rules

### Payment Request Validation
- **merchantId**: Required, non-blank
- **customerId**: Required, non-blank
- **amount**: Required, between 0.01 and 999999.99
- **currency**: Required, exactly 3 characters
- **paymentMethod**: Required, must be valid enum value
- **cardNumber**: Required, 13-19 digits
- **cardHolderName**: Required, 2-100 characters
- **expiryDate**: Required, MM/YY format
- **cvv**: Required, 3-4 digits
- **description**: Optional, max 500 characters

## Error Handling

The API returns structured error responses:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "cardNumber": "Card number must be 13-19 digits",
    "amount": "Amount must be greater than 0"
  }
}
```

## Security Features

- **Card Number Masking**: Only last 4 digits are stored
- **CVV Protection**: CVV is not stored in database
- **Input Validation**: Comprehensive validation for all inputs
- **Transaction ID**: Unique transaction IDs for tracking

## Testing

### Test Payment Scenarios

1. **Successful Payment**: Use any card number not ending with "0000"
2. **Failed Payment**: Use card number ending with "0000"

### Example Test Requests

```bash
# Successful payment
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "MERCH001",
    "customerId": "CUST001",
    "amount": 99.99,
    "currency": "USD",
    "paymentMethod": "CREDIT_CARD",
    "cardNumber": "4111111111111111",
    "cardHolderName": "John Doe",
    "expiryDate": "12/25",
    "cvv": "123",
    "description": "Test payment"
  }'

# Failed payment
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "MERCH001",
    "customerId": "CUST001",
    "amount": 99.99,
    "currency": "USD",
    "paymentMethod": "CREDIT_CARD",
    "cardNumber": "4111111111110000",
    "cardHolderName": "John Doe",
    "expiryDate": "12/25",
    "cvv": "123",
    "description": "Test payment"
  }'
```

## Monitoring

The application includes Spring Boot Actuator for monitoring:

- Health check: `http://localhost:8080/api/actuator/health`
- Application info: `http://localhost:8080/api/actuator/info`
- Metrics: `http://localhost:8080/api/actuator/metrics`

### Payout Endpoints

#### 1. Create Payout (POST)
```http
POST /api/v1/payouts
Content-Type: application/json

{
  "merchantId": "MERCH001",
  "customerId": "CUST001",
  "amount": 500.00,
  "currency": "USD",
  "type": "BANK_TRANSFER",
  "bankAccountNumber": "1234567890",
  "bankRoutingNumber": "021000021",
  "bankName": "Chase Bank",
  "accountHolderName": "John Doe",
  "description": "Commission payout",
  "notes": "Monthly commission payment"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Payout created successfully",
  "payoutId": "POUT-A1B2C3D4",
  "merchantId": "MERCH001",
  "customerId": "CUST001",
  "amount": 500.00,
  "currency": "USD",
  "status": "PENDING",
  "type": "BANK_TRANSFER",
  "maskedBankAccountNumber": "****7890",
  "bankName": "Chase Bank",
  "accountHolderName": "John Doe",
  "description": "Commission payout",
  "gatewayPayoutId": "GW-POUT-E5F6G7H8",
  "processedAt": null,
  "settledAt": null,
  "failureReason": null,
  "notes": "Monthly commission payment",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 2. Get Payout by ID (GET)
```http
GET /api/v1/payouts/1
```

#### 3. Get Payout by Payout ID (GET)
```http
GET /api/v1/payouts/payout-id/POUT-A1B2C3D4
```

#### 4. Get All Payouts (GET)
```http
GET /api/v1/payouts
```

#### 5. Get Payouts by Merchant (GET)
```http
GET /api/v1/payouts/merchant/MERCH001
```

#### 6. Get Payouts by Customer (GET)
```http
GET /api/v1/payouts/customer/CUST001
```

#### 7. Get Payouts by Status (GET)
```http
GET /api/v1/payouts/status/PENDING
```

#### 8. Get Payouts by Type (GET)
```http
GET /api/v1/payouts/type/BANK_TRANSFER
```

#### 9. Update Payout Status (PUT)
```http
PUT /api/v1/payouts/1/status?status=COMPLETED
```

#### 10. Cancel Payout (POST)
```http
POST /api/v1/payouts/1/cancel
```

#### 11. Delete Payout (DELETE)
```http
DELETE /api/v1/payouts/1
```

#### 12. Get Total Payout Amount by Merchant (GET)
```http
GET /api/v1/payouts/merchant/MERCH001/total
```

#### 13. Get Payout Count by Status (GET)
```http
GET /api/v1/payouts/count/status/COMPLETED
```

#### 14. Health Check (GET)
```http
GET /actuator/health
```

### Payout Status Flow
```
PENDING → PROCESSING → COMPLETED
    ↓         ↓           ↓
  CANCELLED  FAILED    REVERSED
```

### Payout Types
- **BANK_TRANSFER**: Standard bank transfer
- **ACH_TRANSFER**: Automated Clearing House transfer
- **WIRE_TRANSFER**: Wire transfer
- **SEPA_TRANSFER**: Single Euro Payments Area transfer
- **SWIFT_TRANSFER**: SWIFT international transfer

### Payout Request Validation
- **merchantId**: Required, 3-50 characters
- **customerId**: Required, 3-50 characters
- **amount**: Required, between 0.01 and 999,999.99
- **currency**: Required, exactly 3 characters (ISO 4217 format)
- **type**: Required, must be valid enum value
- **bankAccountNumber**: Required, 8-34 digits
- **bankRoutingNumber**: Required, 8-12 digits
- **bankName**: Required, 2-100 characters
- **accountHolderName**: Required, 2-100 characters, letters and spaces only
- **description**: Optional, max 500 characters
- **notes**: Optional, max 1000 characters

## Future Enhancements

- Authentication and Authorization
- Rate Limiting
- Payment Gateway Integration
- Webhook Support
- Reporting and Analytics
- Multi-currency Support
- Recurring Payments
- Payment Plans

## License

This project is licensed under the MIT License.
