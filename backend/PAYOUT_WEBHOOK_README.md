# Payout Webhook Sistemi

Bu dokümanda payout webhook sisteminin nasıl çalıştığı açıklanmaktadır.

## Genel Bakış

Payout webhook sistemi, bankalardan gelen payout durumu güncellemelerini alır ve ilgili merchant'lara bildirim gönderir.

## Akış

1. **Banka → Payment Gateway**: Banka payout durumu değiştiğinde webhook gönderir
2. **Payment Gateway**: Webhook'u işler ve payout durumunu günceller
3. **Payment Gateway → Merchant**: Payment ID'ye göre merchant'ı bulur ve bildirim gönderir

## API Endpoints

### 1. Banka Webhook Endpoint

```
POST /v1/bank-webhooks/payouts
```

**Request Body:**
```json
{
  "paymentId": "PAY-123456",
  "payoutId": "POUT-789",
  "status": "COMPLETED",
  "bankName": "Garanti BBVA",
  "message": "Payout completed successfully",
  "failureReason": null,
  "settledAmount": "100.50",
  "settlementDate": "2024-01-15T10:30:00"
}
```

**Response:**
```json
{
  "status": "SUCCESS",
  "message": "Payout webhook processed successfully",
  "paymentId": "PAY-123456",
  "payoutUpdated": true,
  "merchantNotified": true
}
```

### 2. Test Endpoint

```
POST /v1/bank-webhooks/test/payout-status
```

**Request Body:**
```json
{
  "paymentId": "PAY-123456",
  "status": "COMPLETED",
  "bankName": "Test Bank",
  "message": "Test payout completion",
  "settledAmount": "100.50",
  "settlementDate": "2024-01-15T10:30:00"
}
```

## Payout Durumları

- **PENDING**: Beklemede
- **PROCESSING**: İşleniyor
- **COMPLETED**: Tamamlandı
- **FAILED**: Başarısız
- **CANCELLED**: İptal edildi
- **REVERSED**: Geri alındı

## Merchant Bildirimi

Payment Gateway, payout durumu güncellendiğinde merchant'ın webhook URL'ine şu payload'ı gönderir:

```json
{
  "eventType": "PAYOUT_STATUS_UPDATE",
  "paymentId": "PAY-123456",
  "payoutId": "POUT-789",
  "merchantId": "MERCH-001",
  "customerId": "CUST-001",
  "amount": "100.50",
  "currency": "TRY",
  "status": "COMPLETED",
  "message": "Payout completed successfully",
  "bankName": "Garanti BBVA",
  "settledAmount": "100.50",
  "settlementDate": "2024-01-15T10:30:00",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Veritabanı Değişiklikleri

### Payout Tablosu

- `paymentId` field'ı eklendi (Payment ID ile ilişki kurmak için)
- `status` field'ı güncellenebilir
- `processedAt`, `settledAt`, `failureReason` field'ları duruma göre güncellenir

### Merchant Tablosu

- `webhookUrl` field'ı mevcut (Merchant'ın webhook URL'i)

## Güvenlik

- Webhook authentication (header'da banka kimlik doğrulaması)
- HTTPS kullanımı
- IP whitelist (sadece banka IP'lerinden gelen istekler)
- Signature verification

## Hata Yönetimi

- Webhook işleme hatalarında detaylı logging
- Merchant bildirimi başarısız olursa retry mekanizması
- Audit logging tüm işlemler için

## Test Senaryoları

### 1. Başarılı Payout
```bash
curl -X POST http://localhost:8080/v1/bank-webhooks/test/payout-status \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-123456",
    "status": "COMPLETED",
    "bankName": "Test Bank",
    "message": "Payout completed"
  }'
```

### 2. Başarısız Payout
```bash
curl -X POST http://localhost:8080/v1/bank-webhooks/test/payout-status \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-123456",
    "status": "FAILED",
    "bankName": "Test Bank",
    "message": "Insufficient funds",
    "failureReason": "Account balance is insufficient"
  }'
```

## Monitoring

- Tüm webhook istekleri loglanır
- Merchant bildirimleri başarı/başarısızlık oranları takip edilir
- Payout durumu değişiklikleri audit log'a kaydedilir
