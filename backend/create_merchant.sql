-- Create a new merchant for testing
-- Password: password
-- Email: merchant@test.com

INSERT INTO merchants (
    merchant_id,
    merchant_name,
    email,
    password,
    phone,
    address,
    website,
    status,
    api_key,
    secret_key,
    webhook_url,
    webhook_events,
    created_at,
    updated_at
) VALUES (
    'TEST_MERCHANT',
    'Test Merchant',
    'merchant@test.com',
    'password',
    '+90 555 123 4567',
    'Test Address, Istanbul, Turkey',
    'https://testmerchant.com',
    'ACTIVE',
    'pk_merch001_live_abc123',
    'sk_test_merchant_001',
    'https://testmerchant.com/webhooks',
    '["payment.success", "payment.failed", "refund.completed"]',
    NOW(),
    NOW()
);

-- Create Demo Store merchant
-- Password: demo123
-- Email: demo@store.com

INSERT INTO merchants (
    merchant_id,
    merchant_name,
    email,
    password,
    phone,
    address,
    website,
    status,
    api_key,
    secret_key,
    webhook_url,
    webhook_events,
    created_at,
    updated_at
) VALUES (
    'DEMO_STORE',
    'Demo Store',
    'demo@store.com',
    'demo123',
    '+90 555 987 6543',
    'Demo Address, Ankara, Turkey',
    'https://demostore.com',
    'ACTIVE',
    'pk_demo_store_live_def456',
    'sk_demo_store_002',
    'https://demostore.com/webhooks',
    '["payment.success", "payment.failed", "refund.completed", "payout.processed"]',
    NOW(),
    NOW()
);

-- Create Sample Shop merchant
-- Password: sample456
-- Email: sample@shop.com

INSERT INTO merchants (
    merchant_id,
    merchant_name,
    email,
    password,
    phone,
    address,
    website,
    status,
    api_key,
    secret_key,
    webhook_url,
    webhook_events,
    created_at,
    updated_at
) VALUES (
    'SAMPLE_SHOP',
    'Sample Shop',
    'sample@shop.com',
    'sample456',
    '+90 555 111 2222',
    'Sample Address, Izmir, Turkey',
    'https://sampleshop.com',
    'ACTIVE',
    'pk_sample_shop_live_ghi789',
    'sk_sample_shop_003',
    'https://sampleshop.com/webhooks',
    '["payment.success", "payment.failed", "refund.completed", "dispute.created"]',
    NOW(),
    NOW()
);

-- Create Admin Merchant (can see all data)
-- Password: admin123
-- Email: admin@cashflix.com

INSERT INTO merchants (
    merchant_id,
    merchant_name,
    email,
    password,
    phone,
    address,
    website,
    status,
    api_key,
    secret_key,
    webhook_url,
    webhook_events,
    created_at,
    updated_at
) VALUES (
    'ADMIN_MERCHANT',
    'Admin Dashboard',
    'admin@cashflix.com',
    'admin123',
    '+90 555 000 0000',
    'Admin Address, Istanbul, Turkey',
    'https://admin.cashflix.com',
    'ACTIVE',
    'pk_admin_live_admin123',
    'sk_admin_merchant_admin',
    'https://admin.cashflix.com/webhooks',
    '["payment.success", "payment.failed", "refund.completed", "dispute.created"]',
    NOW(),
    NOW()
);

-- Check if all merchants were created successfully
SELECT 
    merchant_id,
    merchant_name,
    email,
    status,
    api_key,
    created_at
FROM merchants 
WHERE email IN ('merchant@test.com', 'demo@store.com', 'sample@shop.com', 'admin@cashflix.com')
ORDER BY created_at;

