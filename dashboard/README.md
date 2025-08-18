# Payment Gateway Dashboard

Modern ve kullanıcı dostu merchant dashboard'u - Hyperswitch benzeri tasarım ile.

## 🚀 Özellikler

### ✅ Tamamlanmış Özellikler
- **🔐 Authentication System** - Email/Password + API Key
- **📊 Dashboard Layout** - Hyperswitch benzeri sidebar ve header
- **💳 Payments Management** - Ödeme listesi ve detay sayfası
- **🔍 Advanced Filtering** - Tarih, durum, ödeme yöntemi filtreleri
- **📈 Statistics Cards** - Ödeme istatistikleri (All, Succeeded, Failed, etc.)
- **🎯 Payment Detail View** - Summary, About Payment, Events & Logs
- **📱 Responsive Design** - Mobile-first yaklaşım
- **🎨 Material-UI Integration** - Modern UI bileşenleri

### 🔄 Mock Data Kullanımı
Şu anda sistem mock data ile çalışıyor. Backend API'leri hazır olduğunda kolayca entegre edilebilir.

## 🛠️ Teknoloji Stack

- **React 18** + **TypeScript**
- **Material-UI v7** (MUI)
- **React Router v7** (Routing)
- **Axios** (HTTP Client)
- **Date-fns** (Date utilities)
- **React Query** (gelecekte API state management için)

## 📋 Kurulum

### 1. Bağımlılıkları Yükle
```bash
cd dashboard
npm install
```

### 2. Geliştirme Sunucusunu Başlat
```bash
npm start
```

Dashboard çalışacak: `http://localhost:3000`

### 3. Demo Giriş Bilgileri
```
Email: merchant@test.com
Password: password
```

## 📁 Proje Yapısı

```
dashboard/
├── src/
│   ├── components/           # UI Bileşenleri
│   │   ├── auth/            # Authentication bileşenleri
│   │   ├── common/          # Ortak bileşenler (StatusChip, etc.)
│   │   ├── layout/          # Layout bileşenleri (Sidebar, Header)
│   │   └── payments/        # Payment özel bileşenleri
│   ├── contexts/            # React Context'ler
│   │   └── AuthContext.tsx  # Authentication state management
│   ├── pages/               # Sayfa bileşenleri
│   │   ├── DashboardHome.tsx
│   │   ├── PaymentsPage.tsx
│   │   └── PaymentDetailPage.tsx
│   ├── services/            # API servisleri
│   │   ├── authApi.ts       # Authentication API
│   │   └── dashboardApi.ts  # Dashboard API
│   ├── types/               # TypeScript type tanımları
│   │   ├── auth.ts
│   │   └── dashboard.ts
│   └── App.tsx              # Ana uygulama
├── public/
└── package.json
```

## 🎯 Sayfa Yapısı

### 1. **Login Page** (`/login`)
- Email/Password authentication
- Demo credentials gösterimi
- Session management

### 2. **Dashboard Home** (`/dashboard`)
- Welcome message
- Quick statistics
- Quick actions
- System status

### 3. **Payments List** (`/dashboard/payments`)
- **Hyperswitch benzeri tasarım**
- Statistics cards (All, Succeeded, Failed, Dropoffs, Cancelled)
- Advanced filtering system
- Payments table with actions
- Pagination
- Export functionality

### 4. **Payment Detail** (`/dashboard/payments/:paymentId`)
- **Summary section** - Amount, status, basic info
- **About Payment section** - Profile, connector, method details
- **Events and Logs accordion** - API events timeline ve request logs
- **Tabs** - Log Details, Request, Response
- Sync button functionality

## 🔗 API Entegrasyonu

### Mevcut Mock Endpoints:
```typescript
// Authentication
authAPI.login(credentials)

// Dashboard
dashboardAPI.getPaymentStats(merchantId)
dashboardAPI.getPayments(merchantId, filters, page, pageSize)
dashboardAPI.getPaymentDetail(paymentId)
dashboardAPI.getPaymentEvents(paymentId)
dashboardAPI.syncPaymentStatus(paymentId)
```

### Backend Entegrasyonu İçin Gerekli Endpoint'ler:
```
POST /api/v1/auth/login
GET  /api/v1/merchant/{merchantId}/payments/stats
GET  /api/v1/merchant/{merchantId}/payments
GET  /api/v1/payments/{paymentId}
GET  /api/v1/payments/{paymentId}/events
POST /api/v1/payments/{paymentId}/sync
```

## 🎨 UI/UX Özellikleri

### Hyperswitch Benzeri Tasarım:
- **Sidebar Navigation** - Collapsible, hierarchical menu
- **Test Mode Banner** - Üst kısımda uyarı
- **Statistics Cards** - Renkli istatistik kartları
- **Advanced Filters** - Expandable filter panel
- **Data Table** - Sortable, actionable table
- **Payment Detail** - Summary + About + Events layout
- **Events Timeline** - Visual API event flow
- **Logs Table** - Detailed request/response logs

### Responsive Design:
- Mobile-first approach
- Collapsible sidebar
- Adaptive grid layouts
- Touch-friendly interactions

## 🔧 Konfigürasyon

### Environment Variables:
```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
REACT_APP_ENVIRONMENT=development
```

### Proxy Configuration:
`package.json` içinde backend proxy:
```json
"proxy": "http://localhost:8080"
```

## 🚀 Production Build

```bash
npm run build
```

Build dosyaları `build/` klasöründe oluşur.

## 🔮 Gelecek Özellikler

### Phase 2 (Yakında):
- [ ] **Refunds Page** - İade yönetimi
- [ ] **Disputes Page** - İtiraz yönetimi  
- [ ] **Customers Page** - Müşteri yönetimi
- [ ] **Analytics Page** - Grafikler ve raporlar
- [ ] **Webhooks Page** - Webhook konfigürasyonu
- [ ] **Settings Page** - API key ve profil ayarları

### Phase 3 (Gelişmiş):
- [ ] **Real-time Updates** - WebSocket entegrasyonu
- [ ] **Export Functionality** - CSV, PDF export
- [ ] **Advanced Charts** - Recharts ile analytics
- [ ] **Notification System** - Toast ve push notifications
- [ ] **Multi-language Support** - i18n entegrasyonu
- [ ] **Dark Mode** - Theme switching

## 🧪 Test

```bash
npm test
```

## 📝 Notlar

### Mock Data Kullanımı:
- Authentication mock credentials ile çalışıyor
- Payment data statik mock veriler
- API calls simüle ediliyor (loading states ile)

### Backend Entegrasyonu:
- `src/services/` altındaki API fonksiyonları hazır
- Mock implementation'ları gerçek API calls ile değiştirilebilir
- Error handling ve loading states hazır

### Hyperswitch Uyumluluğu:
- UI/UX tam uyumlu
- Component yapısı benzer
- Data flow patterns uyumlu

## 🤝 Katkıda Bulunma

1. Feature branch oluştur
2. Değişiklikleri commit et
3. Pull request aç

## 📞 İletişim

Proje ile ilgili sorular için issue açabilirsiniz.

---

**Not**: Bu dashboard, Hyperswitch'in UI/UX tasarımından ilham alınarak oluşturulmuştur ve mevcut Payment Gateway backend'i ile entegre edilmek üzere tasarlanmıştır.
