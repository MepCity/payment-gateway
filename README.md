# 🏦 Payment Gateway - Merchant Authentication System

Modern ve güvenli ödeme gateway sistemi - Merchant authentication ve veri izolasyonu ile.

## 🚀 Özellikler

- ✅ **Spring Boot** backend API
- ✅ **React + Material-UI** frontend
- ✅ **PostgreSQL** veritabanı
- ✅ **Merchant Authentication** sistemi
- ✅ **API Key** bazlı yetkilendirme
- ✅ **Merchant-based** veri izolasyonu
- ✅ **JWT Token** yönetimi
- ✅ **Webhook** sistemi
- ✅ **Dispute** (itiraz) yönetimi
- ✅ **Türk bankası test kartları** desteği
- ✅ **Kart maskelleme** ve güvenlik
- ✅ **BIN tespiti** ve kart markası algılama
- ✅ **External test** yöntemi

## 🔐 Merchant Authentication

### Test Merchant Hesapları

Sistem 3 adet test merchant hesabı ile birlikte gelir:

| Merchant | Email | Password | API Key | Merchant ID |
|----------|-------|----------|---------|-------------|
| Test Merchant Company | test@merchant.com | password123 | pk_test_123456789 | TEST_MERCHANT |
| Demo Online Store | demo@store.com | demo123 | pk_demo_abcdef123 | DEMO_STORE |
| Sample Shop Ltd | contact@sample.com | sample456 | pk_sample_xyz789 | SAMPLE_SHOP |

### Authentication Flow

1. **Login**: Frontend'de merchant email/password ile giriş
2. **JWT Token**: Backend JWT token ve API key döndürür
3. **API Calls**: Tüm API çağrıları `X-API-Key` header'ı ile yapılır
4. **Veri İzolasyonu**: Her merchant sadece kendi verilerini görebilir

### Güvenlik Önlemleri

- ✅ Tüm API endpointleri API key kontrolü yapar
- ✅ Merchant'lar sadece kendi payment/refund/dispute verilerini görebilir
- ✅ Cross-merchant veri erişimi engellenir
- ✅ Audit logging ile tüm işlemler kayıt altına alınır

## 📁 Proje Yapısı

```
payment-gateway/
├── backend/                   # Spring Boot Backend
│   ├── src/main/java/com/payment/gateway/
│   │   ├── controller/        # REST API endpoints
│   │   ├── service/          # Business logic
│   │   ├── model/            # JPA entities
│   │   ├── dto/              # Data transfer objects
│   │   └── repository/       # Data access layer
│   ├── pom.xml
│   └── mvnw
├── frontend/                  # React Frontend
│   ├── src/
│   │   ├── components/       # React components
│   │   ├── services/         # API calls
│   │   └── types/            # TypeScript types
│   ├── package.json
│   └── public/
├── docs/                      # Dokümantasyon
│   └── external-bank-tests.md
└── README.md
```

## 🛠️ Kurulum

### Gereksinimler
- Java 17+
- Node.js 18+
- PostgreSQL 13+
- Maven 3.8+

### 1. Veritabanı Kurulumu
```bash
# PostgreSQL'e bağlan
psql -U postgres

# Veritabanı oluştur
CREATE DATABASE payment_gateway;
CREATE USER payment_user WITH PASSWORD 'payment_pass';
GRANT ALL PRIVILEGES ON DATABASE payment_gateway TO payment_user;
```

### 2. Backend Kurulumu
```bash
cd backend
./mvnw clean install

# Test verilerini yükle
psql -U payment_user -d payment_gateway -f test_data.sql

./mvnw spring-boot:run
```

Backend çalışacak: `http://localhost:8080`

### 3. Frontend Kurulumu  
```bash
cd dashboard
npm install
npm start
```

Frontend çalışacak: `http://localhost:3000`

## 🧪 Test Etme

### 1. Web Dashboard Testi
1. `http://localhost:3000` adresine git
2. Test merchant hesaplarından biriyle giriş yap:
   - Email: `test@merchant.com` / Password: `password123`
   - Email: `demo@store.com` / Password: `demo123`  
   - Email: `contact@sample.com` / Password: `sample456`
3. Dashboard'da merchant'a özel payment, refund, dispute verilerini gör

### 2. API Test (curl)
```bash
# 1. Merchant Authentication
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@merchant.com",
    "password": "password123"
  }'

# 2. API Key ile Payment Test
curl -X POST http://localhost:8080/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-API-Key: pk_test_123456789" \
  -d '{
    "merchantId": "TEST_MERCHANT",
    "customerId": "CUST001",
    "amount": 100.00,
    "currency": "TRY",
    "paymentMethod": "CREDIT_CARD",
    "cardNumber": "4824940000000014",
    "cardHolderName": "TEST KULLANICI",
    "expiryDate": "12/25",
    "cvv": "314",
    "description": "Test ödeme"
  }'
```

### Türk Bankası Test Kartları

#### Garanti BBVA
- ✅ **Başarılı**: `4824 9400 0000 0014` (12/25, CVV: 314)
- ❌ **Yetersiz Bakiye**: `4824 9400 0000 0022` (12/25, CVV: 322)
- 🔐 **3D Secure**: `4824 9400 0000 0030` (12/25, CVV: 330)

#### İş Bankası
- ✅ **Başarılı**: `4508 0345 0803 4509` (01/25, CVV: 123)
- ❌ **Hatalı**: `4508 0345 0803 4517` (01/25, CVV: 123)

### Frontend Test
1. `http://localhost:3000` adresine git
2. "Ödeme Yap" butonuna tıkla
3. Yukarıdaki test kartlarını kullan
4. Sonuçları gözlemle

## 📊 Test Raporu

Detaylı test sonuçları için: [external-bank-tests.md](./external-bank-tests.md)

## 🔗 API Endpoints

### Payments
- `POST /api/v1/payments` - Ödeme oluştur
- `GET /api/v1/payments/{id}` - Ödeme detayı
- `GET /api/v1/payments` - Tüm ödemeler
- `PUT /api/v1/payments/{id}/status` - Ödeme durumu güncelle

### Webhooks
- `POST /api/v1/webhooks` - Webhook oluştur
- `GET /api/v1/webhooks/{id}` - Webhook detayı
- `POST /api/v1/webhooks/delivery` - Webhook gönder

### Disputes
- `POST /api/v1/disputes` - İtiraz oluştur
- `GET /api/v1/disputes/{id}` - İtiraz detayı
- `PUT /api/v1/disputes/{id}/status` - İtiraz durumu güncelle

## 🔐 Güvenlik

- **Kart Maskeleme**: Sadece ilk 6 ve son 4 hane saklanır
- **CVV Korunması**: CVV veritabanında saklanmaz
- **HTTPS**: Tüm iletişim şifreli
- **Input Validation**: Kapsamlı doğrulama

## 🌟 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request açın

## 📝 License

Bu proje MIT License altında lisanslanmıştır.

## 📞 İletişim

Proje ile ilgili sorularınız için issue açabilirsiniz.

---

**Not**: Bu proje Hyperswitch benzeri bir ödeme gateway simülasyonudur. Production ortamında kullanım için ek güvenlik önlemleri alınmalıdır.
