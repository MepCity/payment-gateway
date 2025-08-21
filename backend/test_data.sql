-- Test merchant verileri ve merchant authentication için gerekli veriler
-- Bu dosyayı çalıştırmadan önce ana payment gateway veritabanının hazır olduğundan emin olun

-- Test merchant'ları ekle
INSERT INTO merchants (merchant_id, merchant_name, email, password, phone, address, website, status, api_key, secret_key, webhook_url, created_at, updated_at) VALUES
('TEST_MERCHANT', 'Test Merchant Company', 'test@merchant.com', 'password123', '+90 212 555 0101', 'Test Address, Istanbul, Turkey', 'https://test-merchant.com', 'ACTIVE', 'pk_test_123456789', 'sk_test_987654321', 'https://test-merchant.com/webhook', NOW(), NOW()),
('DEMO_STORE', 'Demo Online Store', 'demo@store.com', 'demo123', '+90 212 555 0102', 'Demo Address, Ankara, Turkey', 'https://demo-store.com', 'ACTIVE', 'pk_demo_abcdef123', 'sk_demo_123fedcba', 'https://demo-store.com/webhook', NOW(), NOW()),
('SAMPLE_SHOP', 'Sample Shop Ltd', 'contact@sample.com', 'sample456', '+90 212 555 0103', 'Sample Address, Izmir, Turkey', 'https://sample-shop.com', 'ACTIVE', 'pk_sample_xyz789', 'sk_sample_789zyx', 'https://sample-shop.com/webhook', NOW(), NOW());

-- Test payment'ları ekle
INSERT INTO payments (payment_id, transaction_id, merchant_id, customer_id, amount, currency, status, payment_method, card_holder_name, card_last_four, card_type, description, bank_response_code, bank_response_message, bank_transaction_id, return_url, cancel_url, ip_address, user_agent, webhook_sent, created_at, updated_at) VALUES
-- TEST_MERCHANT payments
('pay_test_001', 'txn_test_001', 'TEST_MERCHANT', 'cust_001', 100.00, 'TRY', 'COMPLETED', 'CREDIT_CARD', 'Test User', '1234', 'VISA', 'Test Payment 1', '00', 'Approved', 'bank_txn_001', 'https://test.com/success', 'https://test.com/cancel', '192.168.1.1', 'Mozilla/5.0', true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
('pay_test_002', 'txn_test_002', 'TEST_MERCHANT', 'cust_002', 250.50, 'TRY', 'COMPLETED', 'CREDIT_CARD', 'Test User 2', '5678', 'MASTERCARD', 'Test Payment 2', '00', 'Approved', 'bank_txn_002', 'https://test.com/success', 'https://test.com/cancel', '192.168.1.2', 'Mozilla/5.0', true, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
('pay_test_003', 'txn_test_003', 'TEST_MERCHANT', 'cust_003', 75.25, 'TRY', 'FAILED', 'CREDIT_CARD', 'Test User 3', '9012', 'VISA', 'Test Payment 3', '05', 'Do Not Honor', 'bank_txn_003', 'https://test.com/success', 'https://test.com/cancel', '192.168.1.3', 'Mozilla/5.0', false, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours'),

-- DEMO_STORE payments
('pay_demo_001', 'txn_demo_001', 'DEMO_STORE', 'cust_demo_001', 500.00, 'TRY', 'COMPLETED', 'CREDIT_CARD', 'Demo Customer', '1111', 'VISA', 'Demo Store Purchase', '00', 'Approved', 'bank_demo_001', 'https://demo.com/success', 'https://demo.com/cancel', '10.0.0.1', 'Chrome/90.0', true, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
('pay_demo_002', 'txn_demo_002', 'DEMO_STORE', 'cust_demo_002', 150.75, 'TRY', 'PENDING', 'CREDIT_CARD', 'Demo Customer 2', '2222', 'MASTERCARD', 'Demo Store Purchase 2', NULL, NULL, NULL, 'https://demo.com/success', 'https://demo.com/cancel', '10.0.0.2', 'Chrome/90.0', false, NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours'),

-- SAMPLE_SHOP payments
('pay_sample_001', 'txn_sample_001', 'SAMPLE_SHOP', 'cust_sample_001', 299.99, 'TRY', 'COMPLETED', 'CREDIT_CARD', 'Sample Customer', '3333', 'VISA', 'Sample Shop Order', '00', 'Approved', 'bank_sample_001', 'https://sample.com/success', 'https://sample.com/cancel', '172.16.0.1', 'Safari/14.0', true, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');

-- Test refund'ları ekle
INSERT INTO refunds (refund_id, payment_id, transaction_id, merchant_id, amount, currency, status, reason, description, refund_date, processed_date, created_at, updated_at) VALUES
('ref_test_001', 'pay_test_001', 'txn_test_001', 'TEST_MERCHANT', 50.00, 'TRY', 'COMPLETED', 'CUSTOMER_REQUEST', 'Partial refund requested by customer', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
('ref_demo_001', 'pay_demo_001', 'txn_demo_001', 'DEMO_STORE', 100.00, 'TRY', 'PENDING', 'DEFECTIVE_PRODUCT', 'Product was defective', NOW() - INTERVAL '2 hours', NULL, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours');

-- Test dispute'ları ekle
INSERT INTO disputes (dispute_id, payment_id, transaction_id, merchant_id, customer_id, amount, currency, status, reason, description, evidence, dispute_date, resolved_date, created_at, updated_at) VALUES
('dis_test_001', 'pay_test_002', 'txn_test_002', 'TEST_MERCHANT', 'cust_002', 250.50, 'TRY', 'OPENED', 'FRAUD', 'Customer claims they did not make this transaction', 'Customer provided bank statement showing no authorization', NOW() - INTERVAL '12 hours', NULL, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours'),
('dis_demo_001', 'pay_demo_001', 'txn_demo_001', 'DEMO_STORE', 'cust_demo_001', 500.00, 'TRY', 'UNDER_REVIEW', 'PRODUCT_NOT_RECEIVED', 'Customer claims product was never delivered', 'Tracking information shows delivery attempt failed', NOW() - INTERVAL '2 days', NULL, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- API key - Merchant ID eşleşmeleri doğrulama
-- Bu sorguları çalıştırarak verilerinizin doğru girildiğini kontrol edebilirsiniz:
-- SELECT merchant_id, merchant_name, email, api_key, status FROM merchants WHERE status = 'ACTIVE';
-- SELECT payment_id, merchant_id, amount, status, created_at FROM payments ORDER BY created_at DESC;
-- SELECT refund_id, payment_id, merchant_id, amount, status FROM refunds;
-- SELECT dispute_id, payment_id, merchant_id, amount, status FROM disputes;
