# 🏦 Payment Gateway

Modern ve güvenli ödeme gateway sistemi - Türk bankalarının test ortamları ile uyumlu.

## 🚀 Özellikler

- ✅ **Spring Boot** backend API
- ✅ **React + Material-UI** frontend
- ✅ **PostgreSQL** veritabanı
- ✅ **Webhook** sistemi
- ✅ **Dispute** (itiraz) yönetimi
- ✅ **Türk bankası test kartları** desteği
- ✅ **Kart maskelleme** ve güvenlik
- ✅ **BIN tespiti** ve kart markası algılama
- ✅ **External test** yöntemi

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
./mvnw spring-boot:run
```

Backend çalışacak: `http://localhost:8080`

### 3. Frontend Kurulumu
```bash
cd frontend
npm install
npm start
```

Frontend çalışacak: `http://localhost:3000`

## 🧪 Test Etme

### API Test (curl)
```bash
# Başarılı ödeme
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "MERCH001",
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
