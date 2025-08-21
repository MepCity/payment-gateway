package com.payment.gateway.controller;

import com.payment.gateway.dto.MerchantResponse;
import com.payment.gateway.dto.DisputeResponse;
import com.payment.gateway.model.Dispute;
import com.payment.gateway.service.MerchantService;
import com.payment.gateway.service.PaymentService;
import com.payment.gateway.service.DisputeService;
import com.payment.gateway.service.RefundService;
import com.payment.gateway.service.MerchantAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/merchant-dashboard")
@RequiredArgsConstructor
@Slf4j
public class MerchantDashboardController {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    
    private final MerchantService merchantService;
    private final DisputeService disputeService;
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final MerchantAuthService merchantAuthService;
    
    /**
     * Merchant dashboard ana sayfası - Frontend için stats
     */
    @GetMapping("/{merchantId}")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @PathVariable String merchantId,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        log.info("📊 Merchant dashboard getiriliyor: {}", merchantId);

        // API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile dashboard denemesi");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Merchant sadece kendi dashboard'unu görebilir
        String requestingMerchantId = getMerchantIdFromApiKey(apiKey);
        if (requestingMerchantId == null || !requestingMerchantId.equals(merchantId)) {
            log.warn("🚫 Merchant {} tried to access dashboard of {}", requestingMerchantId, merchantId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            // Payment istatistikleri al
            var allPayments = paymentService.getPaymentsByMerchantId(merchantId);
            
            // Refund istatistikleri al
            var allRefunds = refundService.getRefundsByMerchantId(merchantId);
            
            // Dispute istatistikleri al
            var allDisputes = disputeService.getDisputesByMerchantId(merchantId);
            
            // Dashboard stats hesapla
            long totalPayments = allPayments.size();
            double totalAmount = allPayments.stream()
                .mapToDouble(p -> p.getAmount().doubleValue())
                .sum();
            
            long completedPayments = allPayments.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus().name()))
                .count();
            
            double successRate = totalPayments > 0 ? (completedPayments * 100.0 / totalPayments) : 0;
            
            long pendingPayments = allPayments.stream()
                .filter(p -> "PENDING".equals(p.getStatus().name()) || "PROCESSING".equals(p.getStatus().name()))
                .count();
            
            // Unique customers
            long totalCustomers = allPayments.stream()
                .map(p -> p.getCustomerId())
                .distinct()
                .count();
            
            // Refund stats - sadece COMPLETED olan refund'lar
            long totalRefunds = allRefunds.size();
            double refundAmount = allRefunds.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus().name()))
                .mapToDouble(r -> r.getAmount().doubleValue())
                .sum();
            
            // Dispute stats
            long totalDisputes = allDisputes.size();
            long pendingDisputes = allDisputes.stream()
                .filter(d -> "PENDING_MERCHANT_RESPONSE".equals(d.getStatus().name()) || 
                           "OPENED".equals(d.getStatus().name()) ||
                           "UNDER_REVIEW".equals(d.getStatus().name()))
                .count();
            
            double disputeRate = totalPayments > 0 ? (totalDisputes * 100.0 / totalPayments) : 0;
            
            // Dashboard verilerini hazırla
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalPayments", totalPayments);
            stats.put("totalAmount", totalAmount);
            stats.put("successRate", successRate);
            stats.put("pendingPayments", pendingPayments);
            stats.put("totalRefunds", totalRefunds);
            stats.put("refundAmount", refundAmount);
            stats.put("totalCustomers", totalCustomers);
            stats.put("totalDisputes", totalDisputes);
            stats.put("pendingDisputes", pendingDisputes);
            stats.put("disputeRate", disputeRate);
            
            log.info("✅ Dashboard stats calculated for {}: {} payments, {} refunds, {} disputes", 
                merchantId, totalPayments, totalRefunds, totalDisputes);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("❌ Error getting dashboard stats for merchant: {}", merchantId, e);
            
            // Fallback empty stats
            Map<String, Object> emptyStats = new java.util.HashMap<>();
            emptyStats.put("totalPayments", 0);
            emptyStats.put("totalAmount", 0.0);
            emptyStats.put("successRate", 0.0);
            emptyStats.put("pendingPayments", 0);
            emptyStats.put("totalRefunds", 0);
            emptyStats.put("refundAmount", 0.0);
            emptyStats.put("totalCustomers", 0);
            emptyStats.put("totalDisputes", 0);
            emptyStats.put("pendingDisputes", 0);
            emptyStats.put("disputeRate", 0.0);
            
            return ResponseEntity.ok(emptyStats);
        }
    }
    
    /**
     * Merchant profil bilgileri
     */
    @GetMapping("/{merchantId}/profile")
    public ResponseEntity<MerchantResponse> getProfile(@PathVariable String merchantId) {
        log.info("👤 Merchant profil bilgileri getiriliyor: {}", merchantId);
        return merchantService.getMerchantByMerchantId(merchantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Merchant webhook ayarları
     */
    @GetMapping("/{merchantId}/webhook-settings")
    public ResponseEntity<Map<String, Object>> getWebhookSettings(@PathVariable String merchantId) {
        log.info("🔗 Merchant webhook ayarları getiriliyor: {}", merchantId);
        
        MerchantResponse merchant = merchantService.getMerchantByMerchantId(merchantId)
                .orElse(null);
        
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> webhookSettings = new java.util.HashMap<>();
        webhookSettings.put("webhookUrl", merchant.getWebhookUrl());
        webhookSettings.put("webhookEvents", merchant.getWebhookEvents());
        
        return ResponseEntity.ok(webhookSettings);
    }
    
    /**
     * Merchant API bilgileri (sadece API key prefix'i)
     */
    @GetMapping("/{merchantId}/api-info")
    public ResponseEntity<Map<String, Object>> getApiInfo(@PathVariable String merchantId) {
        log.info("🔑 Merchant API bilgileri getiriliyor: {}", merchantId);
        
        MerchantResponse merchant = merchantService.getMerchantByMerchantId(merchantId)
                .orElse(null);
        
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> apiInfo = new java.util.HashMap<>();
        apiInfo.put("merchantId", merchant.getMerchantId());
        apiInfo.put("status", merchant.getStatus());
        apiInfo.put("createdAt", merchant.getCreatedAt());
        
        return ResponseEntity.ok(apiInfo);
    }
    
    /**
     * Merchant aktivite durumu
     */
    @GetMapping("/{merchantId}/activity-status")
    public ResponseEntity<Map<String, Object>> getActivityStatus(@PathVariable String merchantId) {
        log.info("📈 Merchant aktivite durumu getiriliyor: {}", merchantId);
        
        MerchantResponse merchant = merchantService.getMerchantByMerchantId(merchantId)
                .orElse(null);
        
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> activityStatus = new java.util.HashMap<>();
        activityStatus.put("merchantId", merchant.getMerchantId());
        activityStatus.put("status", merchant.getStatus());
        activityStatus.put("lastUpdated", merchant.getUpdatedAt());
        activityStatus.put("isActive", "ACTIVE".equals(merchant.getStatus()));
        
        return ResponseEntity.ok(activityStatus);
    }
    
    /**
     * Merchant dispute'ları - Bekleyen cevaplar
     */
    @GetMapping("/{merchantId}/disputes")
    public ResponseEntity<Map<String, Object>> getMerchantDisputes(
            @PathVariable String merchantId,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        log.info("⚖️ Merchant dispute'ları getiriliyor: {}", merchantId);

        // API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile merchant disputes denemesi");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Merchant sadece kendi dispute'larını görebilir
        String requestingMerchantId = getMerchantIdFromApiKey(apiKey);
        if (requestingMerchantId == null || !requestingMerchantId.equals(merchantId)) {
            log.warn("🚫 Merchant {} tried to access disputes of {}", requestingMerchantId, merchantId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Merchant bilgilerini kontrol et
        MerchantResponse merchant = merchantService.getMerchantByMerchantId(merchantId)
                .orElse(null);
        
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Map<String, Object> disputeData = new java.util.HashMap<>();
            
            // Merchant'ın tüm dispute'larını getir
            List<DisputeResponse> allDisputes = disputeService.getDisputesByMerchantId(merchantId);
            List<DisputeResponse> pendingResponses = disputeService.getPendingDisputeResponses(merchantId);
            
            // Dispute istatistikleri
            long totalDisputes = allDisputes.size();
            long pendingResponseCount = pendingResponses.size();
            long wonDisputes = allDisputes.stream()
                .filter(d -> d.getStatus() == Dispute.DisputeStatus.WON)
                .count();
            long lostDisputes = allDisputes.stream()
                .filter(d -> d.getStatus() == Dispute.DisputeStatus.LOST)
                .count();
            long activeDisputes = allDisputes.stream()
                .filter(d -> d.getStatus() == Dispute.DisputeStatus.OPENED ||
                           d.getStatus() == Dispute.DisputeStatus.UNDER_REVIEW ||
                           d.getStatus() == Dispute.DisputeStatus.EVIDENCE_REQUIRED)
                .count();
            
            // Temel bilgiler
            disputeData.put("merchantId", merchantId);
            disputeData.put("totalDisputes", totalDisputes);
            disputeData.put("pendingResponses", pendingResponseCount);
            disputeData.put("activeDisputes", activeDisputes);
            disputeData.put("wonDisputes", wonDisputes);
            disputeData.put("lostDisputes", lostDisputes);
            
            // Win rate hesapla
            double winRate = (totalDisputes > 0) ? 
                ((double) wonDisputes / (wonDisputes + lostDisputes)) * 100 : 0.0;
            disputeData.put("winRate", Math.round(winRate * 100.0) / 100.0);
            
            // Son güncellenme
            disputeData.put("lastUpdated", LocalDateTime.now().format(DATE_FORMATTER));
            
            // Aktif durum
            disputeData.put("hasActiveDisputes", activeDisputes > 0);
            disputeData.put("needsAttention", pendingResponseCount > 0);
            
            // Yaklaşan deadline'lar
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            long urgentDisputes = pendingResponses.stream()
                .filter(d -> d.getMerchantResponseDeadline() != null &&
                           d.getMerchantResponseDeadline().isBefore(tomorrow))
                .count();
            disputeData.put("urgentDisputes", urgentDisputes);
            
            // Son 30 günün dispute sayısı
            LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
            long recentDisputes = allDisputes.stream()
                .filter(d -> d.getDisputeDate() != null && 
                           d.getDisputeDate().isAfter(lastMonth))
                .count();
            disputeData.put("recentDisputes", recentDisputes);
            
            // Dispute reason dağılımı
            Map<String, Long> reasonBreakdown = allDisputes.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getReason() != null ? d.getReason().toString() : "UNKNOWN",
                    Collectors.counting()
                ));
            disputeData.put("reasonBreakdown", reasonBreakdown);
            
            // Toplam dispute tutarı
            BigDecimal totalDisputeAmount = allDisputes.stream()
                .filter(d -> d.getAmount() != null)
                .map(DisputeResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            disputeData.put("totalDisputeAmount", totalDisputeAmount);
            
            // En son dispute tarihi
            Optional<LocalDateTime> lastDisputeDate = allDisputes.stream()
                .filter(d -> d.getDisputeDate() != null)
                .map(DisputeResponse::getDisputeDate)
                .max(LocalDateTime::compareTo);
            disputeData.put("lastDisputeDate", lastDisputeDate.map(date -> date.format(DATE_FORMATTER)).orElse(null));
            
            // Pending dispute'ların detayları (sadece ID ve deadline)
            List<Map<String, Object>> pendingDetails = pendingResponses.stream()
                .map(d -> {
                    Map<String, Object> detail = new java.util.HashMap<>();
                    detail.put("disputeId", d.getDisputeId());
                    detail.put("amount", d.getAmount());
                    detail.put("currency", d.getCurrency());
                    detail.put("reason", d.getReason());
                    detail.put("deadline", d.getMerchantResponseDeadline() != null ? 
                        d.getMerchantResponseDeadline().format(DATE_FORMATTER) : null);
                    detail.put("status", d.getStatus());
                    return detail;
                })
                .collect(Collectors.toList());
            disputeData.put("pendingDetails", pendingDetails);
            
            log.info("✅ Dispute verileri başarıyla hazırlandı - Merchant: {}, Total: {}, Pending: {}", 
                    merchantId, totalDisputes, pendingResponseCount);
            
            return ResponseEntity.ok(disputeData);
            
        } catch (Exception e) {
            log.error("❌ Dispute verileri getirilirken hata: {}", e.getMessage(), e);
            
            // Hata durumunda fallback verileri
            Map<String, Object> fallbackData = new java.util.HashMap<>();
            fallbackData.put("merchantId", merchantId);
            fallbackData.put("totalDisputes", 0);
            fallbackData.put("pendingResponses", 0);
            fallbackData.put("error", "Dispute verileri yüklenemedi");
            fallbackData.put("lastUpdated", LocalDateTime.now().format(DATE_FORMATTER));
            
            return ResponseEntity.ok(fallbackData);
        }
    }
    
    /**
     * Merchant dispute listesi (sayfalama ile)
     */
    @GetMapping("/{merchantId}/disputes/list")
    public ResponseEntity<Map<String, Object>> getMerchantDisputesList(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        
        log.info("📋 Merchant dispute listesi getiriliyor: {}, page: {}, size: {}", merchantId, page, size);
        
        // Merchant bilgilerini kontrol et
        MerchantResponse merchant = merchantService.getMerchantByMerchantId(merchantId)
                .orElse(null);
        
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Tüm dispute'ları getir
            List<DisputeResponse> allDisputes = disputeService.getDisputesByMerchantId(merchantId);
            
            // Status filtresi uygula
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                try {
                    Dispute.DisputeStatus disputeStatus = Dispute.DisputeStatus.valueOf(status);
                    allDisputes = allDisputes.stream()
                        .filter(d -> d.getStatus() == disputeStatus)
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid dispute status filter: {}", status);
                }
            }
            
            // Sıralama uygula
            if (sortBy != null && !sortBy.isEmpty()) {
                Comparator<DisputeResponse> comparator = null;
                
                switch (sortBy) {
                    case "amount":
                        comparator = Comparator.comparing(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO);
                        break;
                    case "disputeDate":
                        comparator = Comparator.comparing(d -> d.getDisputeDate() != null ? d.getDisputeDate() : LocalDateTime.MIN);
                        break;
                    case "status":
                        comparator = Comparator.comparing(d -> d.getStatus() != null ? d.getStatus().toString() : "");
                        break;
                    case "reason":
                        comparator = Comparator.comparing(d -> d.getReason() != null ? d.getReason().toString() : "");
                        break;
                    default:
                        comparator = Comparator.comparing(d -> d.getDisputeDate() != null ? d.getDisputeDate() : LocalDateTime.MIN);
                        break;
                }
                
                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }
                
                allDisputes = allDisputes.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
            } else {
                // Varsayılan olarak tarih bazında ters sıralama
                allDisputes = allDisputes.stream()
                    .sorted((d1, d2) -> {
                        LocalDateTime date1 = d1.getDisputeDate() != null ? d1.getDisputeDate() : LocalDateTime.MIN;
                        LocalDateTime date2 = d2.getDisputeDate() != null ? d2.getDisputeDate() : LocalDateTime.MIN;
                        return date2.compareTo(date1);
                    })
                    .collect(Collectors.toList());
            }
            
            // Sayfalama uygula
            int totalElements = allDisputes.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);
            
            List<DisputeResponse> pagedDisputes = allDisputes.subList(startIndex, endIndex);
            
            // Response oluştur
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("content", pagedDisputes);
            response.put("page", page);
            response.put("size", size);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("first", page == 0);
            response.put("last", page >= totalPages - 1);
            response.put("empty", pagedDisputes.isEmpty());
            
            // Filtreleme ve sıralama bilgileri
            response.put("appliedFilters", Map.of(
                "status", status != null ? status : "ALL",
                "sortBy", sortBy != null ? sortBy : "disputeDate",
                "sortDir", sortDir != null ? sortDir : "desc"
            ));
            
            log.info("✅ Dispute listesi başarıyla hazırlandı - Merchant: {}, Total: {}, Page: {}/{}", 
                    merchantId, totalElements, page + 1, totalPages);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Dispute listesi getirilirken hata: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("content", List.of());
            errorResponse.put("page", page);
            errorResponse.put("size", size);
            errorResponse.put("totalElements", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("error", "Dispute listesi yüklenemedi: " + e.getMessage());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * Merchant dispute cevabı formu
     */
    @PostMapping("/{merchantId}/disputes/{disputeId}/respond")
    public ResponseEntity<Map<String, Object>> respondToDispute(
            @PathVariable String merchantId,
            @PathVariable String disputeId,
            @RequestBody Map<String, Object> response) {
        
        log.info("📝 Merchant dispute cevabı - Merchant: {}, Dispute: {}, Response: {}", 
                merchantId, disputeId, response.get("responseType"));
        
        Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            String responseType = (String) response.get("responseType"); // ACCEPT veya DEFEND
            // String evidence = (String) response.get("evidence");
            // String notes = (String) response.get("notes");
            
            if (responseType == null || (!responseType.equals("ACCEPT") && !responseType.equals("DEFEND"))) {
                result.put("success", false);
                result.put("message", "Invalid response type. Must be ACCEPT or DEFEND");
                return ResponseEntity.badRequest().body(result);
            }
            
            // DisputeService'e gönder
            // disputeService.submitMerchantResponse(disputeId, responseType, evidence, notes);
            
            log.info("✅ Merchant response submitted successfully - Dispute: {}, Type: {}", 
                    disputeId, responseType);
            
            result.put("success", true);
            result.put("message", "Response submitted successfully");
            result.put("disputeId", disputeId);
            result.put("responseType", responseType);
            result.put("nextStep", responseType.equals("ACCEPT") ? 
                "Automatic refund will be processed" : 
                "Evidence will be reviewed by admin team");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Error submitting merchant response", e);
            result.put("success", false);
            result.put("message", "Failed to submit response: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Merchant dispute detayı - Tek dispute bilgisi
     */
    @GetMapping("/{merchantId}/disputes/{disputeId}")
    public ResponseEntity<DisputeResponse> getMerchantDisputeDetail(
            @PathVariable String merchantId,
            @PathVariable String disputeId) {
        
        log.info("🔍 Merchant dispute detayı getiriliyor - Merchant: {}, Dispute: {}", merchantId, disputeId);
        
        // Merchant bilgilerini kontrol et
        MerchantResponse merchant = merchantService.getMerchantByMerchantId(merchantId)
                .orElse(null);
        
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Dispute'ı disputeId ile getir (String olarak)
            DisputeResponse dispute = disputeService.getDisputeByDisputeId(disputeId);
            
            if (dispute == null) {
                log.warn("❌ Dispute bulunamadı: {}", disputeId);
                return ResponseEntity.notFound().build();
            }
            
            // Dispute'ın bu merchant'a ait olduğunu kontrol et
            if (!merchantId.equals(dispute.getMerchantId())) {
                log.warn("❌ Dispute bu merchant'a ait değil - Dispute: {}, Expected: {}, Found: {}", 
                    disputeId, merchantId, dispute.getMerchantId());
                return ResponseEntity.notFound().build();
            }
            
            log.info("✅ Dispute detayı başarıyla getirildi - Dispute: {}", disputeId);
            
            return ResponseEntity.ok(dispute);
            
        } catch (Exception e) {
            log.error("❌ Dispute detayı getirilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API key'den merchant ID'yi çıkart
     */
    private String getMerchantIdFromApiKey(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        
        // Test mode - her test API key'ini farklı merchant'a eşle
        if (apiKey.startsWith("pk_test_")) {
            switch (apiKey) {
                case "pk_test_merchant1":
                    return "TEST_MERCHANT";
                case "pk_test_merchant2":
                    return "TEST_MERCHANT_2";
                case "pk_test_merchant3":
                    return "TEST_MERCHANT_3";
                default:
                    return "TEST_MERCHANT"; // Default test merchant
            }
        }
        
        // Production'da merchant'ı API key ile bulup merchant ID'yi döneriz
        return merchantAuthService.getMerchantByApiKey(apiKey)
                .map(merchant -> merchant.getMerchantId())
                .orElse(null);
    }
}