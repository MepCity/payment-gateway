# 🔐 Merchant Authentication System Setup Guide

Bu dosya, Payment Gateway sistemine merchant authentication özelliklerinin nasıl entegre edildiğini ve nasıl test edileceğini açıklar.

## 📋 Yapılan Değişiklikler

### 1. Backend Değişiklikleri

#### Model Katmanı
- ✅ `Merchant.java` - password alanı zaten mevcuttu
- ✅ `LoginRequest.java` - Email/password DTO
- ✅ `LoginResponse.java` - JWT token ve user bilgileri DTO

#### Service Katmanı  
- ✅ `MerchantAuthService.java` - Authentication ve JWT token yönetimi
- ✅ `PaymentService.java` - Merchant-based filtering methodları eklendi
- ✅ `RefundService.java` - Merchant-based filtering methodları eklendi
- ✅ `DisputeService.java` - Merchant-based filtering methodları eklendi

#### Controller Katmanı
- ✅ `AuthController.java` - POST /v1/auth/login endpoint'i
- ✅ `PaymentController.java` - Tüm endpointlere API key kontrolü eklendi
- ✅ `RefundController.java` - Tüm endpointlere API key kontrolü eklendi
- ✅ `DisputeController.java` - Tüm endpointlere API key kontrolü eklendi
- ✅ `MerchantDashboardController.java` - API key kontrolü eklendi

### 2. Frontend Değişiklikleri

#### API Katmanı
- ✅ `authApi.ts` - Backend login endpoint'ine yönlendirildi
- ✅ `paymentApi.ts` - API çağrılarına X-API-Key header'ı eklendi
- ✅ `dashboardApi.ts` - API çağrılarına X-API-Key header'ı eklendi

#### UI Katmanı
- ✅ `LoginPage.tsx` - Test merchant bilgileri eklendi
- ✅ `AuthContext.tsx` - JWT token ve API key yönetimi

### 3. Test Verileri
- ✅ `test_data.sql` - 3 test merchant hesabı ve örnek payment verileri

## 🚀 Hızlı Başlangıç

### 1. Backend Kurulumu
```bash
cd backend
./mvnw clean install

# Test verilerini yükle
psql -U payment_user -d payment_gateway -f test_data.sql

./mvnw spring-boot:run
```

### 2. Frontend Kurulumu
```bash
cd dashboard
npm install
npm start
```

### 3. Test Login
- URL: http://localhost:3000
- Test Hesapları:
  - Email: `test@merchant.com` / Password: `password123`
  - Email: `demo@store.com` / Password: `demo123`
  - Email: `contact@sample.com` / Password: `sample456`

## 🔍 API Endpoints

### Authentication
```bash
# Login
POST /v1/auth/login
Content-Type: application/json

{
  "email": "test@merchant.com",
  "password": "password123"
}

# Response
{
  "success": true,
  "message": "Login successful",
  "user": {
    "id": "1",
    "email": "test@merchant.com",
    "merchantId": "TEST_MERCHANT",
    "merchantName": "Test Merchant Company",
    "role": "MERCHANT",
    "apiKey": "pk_test_123456789"
  },
  "token": "jwt_token_...",
  "apiKey": "pk_test_123456789"
}
```

### Protected Endpoints
Tüm payment, refund, dispute endpointleri artık API key gerektirir:

```bash
# Payment List
GET /v1/payments
X-API-Key: pk_test_123456789

# Payment by ID  
GET /v1/payments/1
X-API-Key: pk_test_123456789

# Create Payment
POST /v1/payments
X-API-Key: pk_test_123456789
Content-Type: application/json

# Dashboard Stats
GET /v1/merchant-dashboard/TEST_MERCHANT
X-API-Key: pk_test_123456789
```

## 🛡️ Güvenlik Özellikleri

### API Key Kontrolü
- Her endpoint'e gelen istekte X-API-Key header'ı kontrol edilir
- Geçersiz API key ile HTTP 401 Unauthorized döner
- Test mode için pk_test_ ile başlayan keyler kabul edilir

### Merchant İzolasyonu
- Her merchant sadece kendi verilerini görebilir
- Cross-merchant veri erişimi HTTP 403 Forbidden ile engellenir
- Payment, refund, dispute listeleri merchant'a özel filtrelenir

### Audit Logging
- Tüm authentication denemesi loglanır
- API key validation olayları kaydedilir
- Başarısız erişim denemeleri izlenir

## 🧪 Test Senaryoları

### 1. Login Testi
```bash
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@merchant.com",
    "password": "password123"
  }'
```

### 2. API Key ile Payment Listesi
```bash
curl -X GET http://localhost:8080/v1/payments \
  -H "X-API-Key: pk_test_123456789"
```

### 3. Merchant İzolasyon Testi
```bash
# TEST_MERCHANT API key ile DEMO_STORE dashboard'una erişim denemesi (403 döner)
curl -X GET http://localhost:8080/v1/merchant-dashboard/DEMO_STORE \
  -H "X-API-Key: pk_test_123456789"
```

### 4. Geçersiz API Key Testi
```bash
# Geçersiz API key (401 döner)
curl -X GET http://localhost:8080/v1/payments \
  -H "X-API-Key: invalid_key"
```

## 🔧 Troubleshooting

### Backend Hataları
- **Connection refused**: Backend çalışıyor mu? `./mvnw spring-boot:run`
- **Database error**: PostgreSQL çalışıyor mu? Test verileri yüklendi mi?
- **401 Unauthorized**: API key header'ı eksik veya geçersiz
- **403 Forbidden**: Merchant farklı merchant'ın verisine erişmeye çalışıyor

### Frontend Hataları
- **Login failed**: Backend çalışıyor mu? Doğru test credentials kullanıldı mı?
- **API calls fail**: X-API-Key header'ı localStorage'dan doğru alınıyor mu?
- **CORS error**: Backend CORS ayarları doğru mu?

### Database Sorunları
```bash
# Test verilerini kontrol et
psql -U payment_user -d payment_gateway -c "SELECT merchant_id, email FROM merchants;"

# Payment verilerini kontrol et
psql -U payment_user -d payment_gateway -c "SELECT payment_id, merchant_id, amount FROM payments;"
```

## 📝 Geliştirme Notları

### Production'a Geçiş İçin
1. **JWT Secret**: Güvenli JWT secret key kullan
2. **Password Hashing**: Bcrypt ile password hash'le
3. **API Key Generation**: Secure random API key generation
4. **Rate Limiting**: API endpoint'leri için rate limiting ekle
5. **HTTPS**: TLS/SSL sertifikası kullan

### Monitoring
- Authentication başarı/başarısızlık oranları
- API key kullanım istatistikleri  
- Merchant bazlı işlem hacimleri
- Güvenlik olayları (failed login, invalid API key, etc.)

## 🎯 Sonraki Adımlar

1. **Multi-factor Authentication**: SMS/Email OTP ekleme
2. **API Key Rotation**: Periyodik API key yenileme
3. **Role-based Access**: Admin, merchant, operator rolleri
4. **IP Whitelist**: Merchant IP kısıtlamaları
5. **Webhook Authentication**: Webhook endpoint'leri için authentication
