package com.payment.gateway.controller;

import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.dto.ThreeDSecureResponse;
import com.payment.gateway.service.ThreeDSecureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 3D Secure Controller
 * Otomatik bank detection ve multi-format support
 */
@RestController
@RequestMapping("/v1/3dsecure")
@RequiredArgsConstructor
@Slf4j
public class ThreeDSecureController {
    
    private final ThreeDSecureService threeDSecureService;
    
    /**
     * 3D Secure başlat (otomatik bank detection)
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiate3DSecure(
            @Valid @RequestBody PaymentRequest paymentRequest,
            HttpServletRequest request) {
        
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        log.info("3D Secure initiation request - Card: {}, IP: {}", 
                maskCardNumber(paymentRequest.getCardNumber()), clientIp);
        
        try {
            // Otomatik bank detection ve 3D Secure başlatma
            ThreeDSecureResponse response = threeDSecureService.initiate3DSecure(
                    paymentRequest, clientIp, userAgent);
            
            if (response.isSuccess()) {
                log.info("3D Secure initiated successfully - Bank: {}, Status: {}", 
                        response.getBankName(), response.getStatus());
                
                // Eğer HTML form data varsa, HTML olarak döndür
                if (response.getFormData() != null) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.TEXT_HTML)
                            .body(response.getFormData());
                }
                
                // JSON response döndür
                return ResponseEntity.ok(response);
                
            } else {
                log.warn("3D Secure initiation failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error initiating 3D Secure: {}", e.getMessage(), e);
            
            ThreeDSecureResponse errorResponse = ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("3D Secure başlatma hatası: " + e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Kart için 3D Secure desteği kontrolü
     */
    @PostMapping("/check-support")
    public ResponseEntity<Map<String, Object>> checkSupport(@RequestBody Map<String, String> request) {
        String cardNumber = request.get("cardNumber");
        
        log.debug("Checking 3D Secure support for card: {}", maskCardNumber(cardNumber));
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean supported = threeDSecureService.is3DSecureSupported(cardNumber);
            String bankName = threeDSecureService.detectBankName(cardNumber);
            var requestFormat = threeDSecureService.detectRequestFormat(cardNumber);
            
            response.put("supported", supported);
            response.put("bankName", bankName);
            response.put("requestFormat", requestFormat != null ? requestFormat.name() : null);
            response.put("cardBin", cardNumber.length() >= 6 ? cardNumber.substring(0, 6) : null);
            
            log.info("3D Secure support check - Card: {}, Supported: {}, Bank: {}", 
                    maskCardNumber(cardNumber), supported, bankName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking 3D Secure support: {}", e.getMessage());
            response.put("error", "Support check failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Garanti BBVA 3D Secure callback
     */
    @PostMapping("/callback/garanti_bbva")
    public ResponseEntity<String> handleGarantiCallback(HttpServletRequest request) {
        return handleBankCallback("GARANTI_BBVA", request);
    }
    
    /**
     * İş Bankası 3D Secure callback
     */
    @PostMapping("/callback/isbank")
    public ResponseEntity<String> handleIsBankCallback(HttpServletRequest request) {
        return handleBankCallback("ISBANK", request);
    }
    
    /**
     * Yapı Kredi 3D Secure callback
     */
    @PostMapping("/callback/yapikredi")
    public ResponseEntity<String> handleYapiKrediCallback(HttpServletRequest request) {
        return handleBankCallback("YAPIKREDI", request);
    }
    
    /**
     * Generic bank callback handler
     */
    @PostMapping("/callback/{bankName}")
    public ResponseEntity<String> handleGenericCallback(
            @PathVariable String bankName, 
            HttpServletRequest request) {
        return handleBankCallback(bankName.toUpperCase(), request);
    }
    
    /**
     * 3D Secure success page
     */
    @GetMapping("/success")
    public ResponseEntity<String> success3DSecure(@RequestParam Map<String, String> params) {
        log.info("3D Secure success callback received with params: {}", params);
        
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>3D Secure Başarılı</title>
                <meta charset="UTF-8">
            </head>
            <body>
                <div style="text-align: center; margin-top: 100px;">
                    <h1 style="color: green;">✅ 3D Secure Doğrulama Başarılı</h1>
                    <p>Ödemeniz güvenli bir şekilde doğrulandı.</p>
                    <p>Lütfen bekleyiniz, yönlendiriliyorsunuz...</p>
                </div>
                <script>
                    setTimeout(function() {
                        window.close();
                    }, 3000);
                </script>
            </body>
            </html>
            """;
        
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
    
    /**
     * 3D Secure fail page
     */
    @GetMapping("/fail")
    public ResponseEntity<String> fail3DSecure(@RequestParam Map<String, String> params) {
        log.warn("3D Secure fail callback received with params: {}", params);
        
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>3D Secure Başarısız</title>
                <meta charset="UTF-8">
            </head>
            <body>
                <div style="text-align: center; margin-top: 100px;">
                    <h1 style="color: red;">❌ 3D Secure Doğrulama Başarısız</h1>
                    <p>Güvenlik doğrulaması tamamlanamadı.</p>
                    <p>Lütfen tekrar deneyin veya farklı bir kart kullanın.</p>
                </div>
                <script>
                    setTimeout(function() {
                        window.close();
                    }, 3000);
                </script>
            </body>
            </html>
            """;
        
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
    
    /**
     * 3D Secure adapter statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getStats() {
        try {
            Object stats = threeDSecureService.getAdapterStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting adapter stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Stats retrieval failed: " + e.getMessage()));
        }
    }
    
    /**
     * 3D Secure health check
     */
    @GetMapping("/health")
    public ResponseEntity<Object> healthCheck() {
        try {
            Object healthStatus = threeDSecureService.performHealthCheck();
            return ResponseEntity.ok(healthStatus);
        } catch (Exception e) {
            log.error("Error performing health check: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Health check failed: " + e.getMessage()));
        }
    }
    
    /**
     * Bank callback handler (private)
     */
    private ResponseEntity<String> handleBankCallback(String bankName, HttpServletRequest request) {
        log.info("Handling 3D Secure callback from bank: {}", bankName);
        
        try {
            // Request body'yi al
            String callbackData = getRequestBody(request);
            
            log.debug("Callback data received from {}: {}", bankName, callbackData);
            
            // ThreeDSecureService ile callback'i işle
            ThreeDSecureResponse response = threeDSecureService.handle3DCallback(bankName, callbackData);
            
            if (response.isSuccess()) {
                log.info("3D Secure callback processed successfully - Bank: {}, Status: {}", 
                        bankName, response.getStatus());
                
                // Success sayfasına yönlendir
                String redirectHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>3D Secure Tamamlandı</title>
                    </head>
                    <body>
                        <script>
                            window.location.href = '/api/v1/3dsecure/success';
                        </script>
                    </body>
                    </html>
                    """;
                
                return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(redirectHtml);
                
            } else {
                log.warn("3D Secure callback failed - Bank: {}, Error: {}", bankName, response.getMessage());
                
                // Fail sayfasına yönlendir
                String redirectHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>3D Secure Başarısız</title>
                    </head>
                    <body>
                        <script>
                            window.location.href = '/api/v1/3dsecure/fail';
                        </script>
                    </body>
                    </html>
                    """;
                
                return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(redirectHtml);
            }
            
        } catch (Exception e) {
            log.error("Error handling 3D Secure callback from {}: {}", bankName, e.getMessage(), e);
            
            String errorHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>3D Secure Hata</title>
                </head>
                <body>
                    <div style="text-align: center; margin-top: 100px;">
                        <h1 style="color: red;">Hata Oluştu</h1>
                        <p>3D Secure işlemi sırasında bir hata oluştu.</p>
                    </div>
                </body>
                </html>
                """;
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_HTML)
                    .body(errorHtml);
        }
    }
    
    /**
     * Request body'yi string olarak al
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            log.error("Error reading request body: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Client IP address al
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Kart numarasını maskele
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}
