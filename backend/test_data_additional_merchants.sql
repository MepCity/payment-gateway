-- TEST_MERCHANT_2 ve TEST_MERCHANT_3 için test verileri

-- TEST_MERCHANT_2 için veriler
INSERT INTO merchants (merchant_id, name, email, status, api_key, created_at, password) VALUES 
('TEST_MERCHANT_2', 'Test Merchant 2', 'merchant2@test.com', 'ACTIVE', 'pk_test_merchant2', NOW(), '$2a$10$mz8ZqGfzHWx8xQV5M9C5WeJ1g2.p8x8xf5M6jZL2QJ.mN1rN5p5Ku') -- password: merchant2_password
ON CONFLICT (merchant_id) DO NOTHING;

INSERT INTO merchants (merchant_id, name, email, status, api_key, created_at, password) VALUES 
('TEST_MERCHANT_3', 'Test Merchant 3', 'merchant3@test.com', 'ACTIVE', 'pk_test_merchant3', NOW(), '$2a$10$mz8ZqGfzHWx8xQV5M9C5WeJ1g2.p8x8xf5M6jZL2QJ.mN1rN5p5Ku') -- password: merchant3_password
ON CONFLICT (merchant_id) DO NOTHING;

-- TEST_MERCHANT_2 için örnek payment'lar
INSERT INTO payments (payment_id, transaction_id, merchant_id, customer_id, amount, currency, status, payment_method, card_number, card_holder_name, card_brand, card_bin, card_last_four, expiry_date, description, gateway_response, gateway_transaction_id, created_at) VALUES
('PAY-MERCHANT2-001', 'TXN-MERCHANT2-001', 'TEST_MERCHANT_2', 'customer_merchant2_001', 150.00, 'TRY', 'COMPLETED', 'CREDIT_CARD', '4111********1111', 'Merchant2 Customer1', 'VISA', '411111', '1111', '12/26', 'Merchant 2 Test Payment 1', 'Payment completed successfully', 'GTW-MERCHANT2-001', NOW()),
('PAY-MERCHANT2-002', 'TXN-MERCHANT2-002', 'TEST_MERCHANT_2', 'customer_merchant2_002', 75.50, 'TRY', 'COMPLETED', 'CREDIT_CARD', '5555********4444', 'Merchant2 Customer2', 'MASTERCARD', '555555', '4444', '12/26', 'Merchant 2 Test Payment 2', 'Payment completed successfully', 'GTW-MERCHANT2-002', NOW()),
('PAY-MERCHANT2-003', 'TXN-MERCHANT2-003', 'TEST_MERCHANT_2', 'customer_merchant2_003', 200.00, 'TRY', 'FAILED', 'CREDIT_CARD', '4111********2222', 'Merchant2 Customer3', 'VISA', '411111', '2222', '12/26', 'Merchant 2 Test Payment 3', 'Payment failed - insufficient funds', 'GTW-MERCHANT2-003', NOW());

-- TEST_MERCHANT_3 için örnek payment'lar
INSERT INTO payments (payment_id, transaction_id, merchant_id, customer_id, amount, currency, status, payment_method, card_number, card_holder_name, card_brand, card_bin, card_last_four, expiry_date, description, gateway_response, gateway_transaction_id, created_at) VALUES
('PAY-MERCHANT3-001', 'TXN-MERCHANT3-001', 'TEST_MERCHANT_3', 'customer_merchant3_001', 500.00, 'TRY', 'COMPLETED', 'CREDIT_CARD', '4111********3333', 'Merchant3 Customer1', 'VISA', '411111', '3333', '12/26', 'Merchant 3 Test Payment 1', 'Payment completed successfully', 'GTW-MERCHANT3-001', NOW()),
('PAY-MERCHANT3-002', 'TXN-MERCHANT3-002', 'TEST_MERCHANT_3', 'customer_merchant3_002', 125.75, 'TRY', 'PROCESSING', 'CREDIT_CARD', '5555********5555', 'Merchant3 Customer2', 'MASTERCARD', '555555', '5555', '12/26', 'Merchant 3 Test Payment 2', 'Payment processing', 'GTW-MERCHANT3-002', NOW());

-- TEST_MERCHANT_2 için örnek dispute'lar
INSERT INTO disputes (dispute_id, payment_id, transaction_id, merchant_id, customer_id, amount, currency, status, reason, description, gateway_response, gateway_dispute_id, dispute_date, created_at) VALUES
('DSP-MERCHANT2-001', 'PAY-MERCHANT2-001', 'TXN-MERCHANT2-001', 'TEST_MERCHANT_2', 'customer_merchant2_001', 150.00, 'TRY', 'PENDING_MERCHANT_RESPONSE', 'UNAUTHORIZED_TRANSACTION', 'Customer claims card was stolen', 'Dispute opened - pending merchant response', 'GDSP-merchant2-001', NOW(), NOW()),
('DSP-MERCHANT2-002', 'PAY-MERCHANT2-002', 'TXN-MERCHANT2-002', 'TEST_MERCHANT_2', 'customer_merchant2_002', 75.50, 'TRY', 'UNDER_REVIEW', 'NON_RECEIPT', 'Product not received by customer', 'Dispute under review', 'GDSP-merchant2-002', NOW(), NOW());

-- TEST_MERCHANT_3 için örnek dispute'lar
INSERT INTO disputes (dispute_id, payment_id, transaction_id, merchant_id, customer_id, amount, currency, status, reason, description, gateway_response, gateway_dispute_id, dispute_date, created_at) VALUES
('DSP-MERCHANT3-001', 'PAY-MERCHANT3-001', 'TXN-MERCHANT3-001', 'TEST_MERCHANT_3', 'customer_merchant3_001', 500.00, 'TRY', 'OPENED', 'DEFECTIVE_PRODUCT', 'Product delivered damaged', 'Dispute opened successfully', 'GDSP-merchant3-001', NOW(), NOW());

-- TEST_MERCHANT_2 için örnek refund'lar
INSERT INTO refunds (refund_id, payment_id, transaction_id, merchant_id, customer_id, amount, currency, status, reason, description, gateway_response, gateway_refund_id, refund_date, created_at) VALUES
('REF-MERCHANT2-001', 'PAY-MERCHANT2-001', 'TXN-MERCHANT2-001', 'TEST_MERCHANT_2', 'customer_merchant2_001', 50.00, 'TRY', 'COMPLETED', 'CUSTOMER_REQUEST', 'Partial refund due to late delivery', 'Refund completed successfully', 'GREF-merchant2-001', NOW(), NOW());

-- TEST_MERCHANT_3 için örnek refund'lar
INSERT INTO refunds (refund_id, payment_id, transaction_id, merchant_id, customer_id, amount, currency, status, reason, description, gateway_response, gateway_refund_id, refund_date, created_at) VALUES
('REF-MERCHANT3-001', 'PAY-MERCHANT3-001', 'TXN-MERCHANT3-001', 'TEST_MERCHANT_3', 'customer_merchant3_001', 100.00, 'TRY', 'PROCESSING', 'DEFECTIVE_PRODUCT', 'Refund for damaged product', 'Refund processing', 'GREF-merchant3-001', NOW(), NOW());

COMMIT;
