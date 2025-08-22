package com.payment.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock Garanti BBVA API Controller
 * Gerçek Garanti BBVA test ortamını simüle eder
 */
@RestController
@RequestMapping("/mock/garanti")
@Slf4j
@CrossOrigin(origins = "*")
public class MockGarantiController {

    /**
     * Garanti BBVA ödeme API'sini simüle eder
     */
    @PostMapping("/api/v1/payments")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> request) {
        log.info("Mock Garanti API called with request: {}", request);
        
        Map<String, Object> response = new HashMap<>();
        String cardNumber = (String) request.get("cardNumber");
        String cvv = (String) request.get("cvv");
        Integer amount = (Integer) request.get("amount");
        String orderId = (String) request.get("orderId");
        
        // CVV kontrolü (güvenlik için)
        if (cvv == null || cvv.length() < 3) {
            response.put("resultCode", "30");
            response.put("resultMessage", "Invalid CVV");
            response.put("orderId", orderId);
            log.warn("Invalid CVV provided for order: {}", orderId);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Gerçek Garanti test kartlarının davranışını simüle et
        if (cardNumber != null) {
            if (cardNumber.endsWith("0014") && "314".equals(cvv)) {
                // Başarılı işlem - doğru CVV
                response.put("resultCode", "00");
                response.put("resultMessage", "Approved");
                response.put("transactionId", "GRT-" + System.currentTimeMillis());
                response.put("authCode", "123456");
                response.put("orderId", orderId);
                response.put("amount", amount);
                
            } else if (cardNumber.endsWith("0014") && !"314".equals(cvv)) {
                // Yanlış CVV
                response.put("resultCode", "14");
                response.put("resultMessage", "Invalid CVV");
                response.put("orderId", orderId);
                
            } else if (cardNumber.endsWith("0022") && "322".equals(cvv)) {
                // Yetersiz bakiye - doğru CVV
                response.put("resultCode", "51");
                response.put("resultMessage", "Insufficient funds");
                response.put("orderId", orderId);
                
            } else if (cardNumber.endsWith("0030") && "330".equals(cvv)) {
                // 3D Secure gerekli - doğru CVV
                response.put("resultCode", "3D");
                response.put("resultMessage", "3D Secure authentication required");
                response.put("threeDUrl", "http://localhost:8080/api/mock/garanti/3d-secure?orderId=" + orderId);
                response.put("orderId", orderId);
                
            } else {
                // Yanlış kart numarası veya CVV
                response.put("resultCode", "99");
                response.put("resultMessage", "Invalid card number or CVV");
                response.put("orderId", orderId);
            }
        } else {
            response.put("resultCode", "30");
            response.put("resultMessage", "Invalid request");
        }
        
        log.info("Mock Garanti API response: {}", response);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 3D Secure sayfasını simüle eder
     */
    @GetMapping("/3d-secure")
    public ResponseEntity<String> show3DSecurePage(@RequestParam String orderId) {
        log.info("Mock 3D Secure page for order: {}", orderId);
        
        String html = """
            <html>
            <head>
                <title>Garanti BBVA 3D Secure</title>
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                    .container { max-width: 400px; margin: 0 auto; }
                    button { padding: 10px 20px; margin: 10px; font-size: 16px; }
                    .success { background-color: #4CAF50; color: white; }
                    .fail { background-color: #f44336; color: white; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>🔐 Garanti BBVA 3D Secure</h2>
                    <p>Order ID: %s</p>
                    <p>Test için aşağıdaki butonlardan birini seçin:</p>
                    
                    <button class="success" onclick="success()">✅ Başarılı Doğrulama</button>
                    <button class="fail" onclick="fail()">❌ Başarısız Doğrulama</button>
                    
                    <script>
                        function success() {
                            const form = document.createElement('form');
                            form.method = 'POST';
                            form.action = '/api/v1/payments/3d-callback/success';
                            
                            const orderIdInput = document.createElement('input');
                            orderIdInput.type = 'hidden';
                            orderIdInput.name = 'orderId';
                            orderIdInput.value = '%s';
                            form.appendChild(orderIdInput);
                            
                            const transactionIdInput = document.createElement('input');
                            transactionIdInput.type = 'hidden';
                            transactionIdInput.name = 'transactionId';
                            transactionIdInput.value = 'GRT-3DS-' + Date.now();
                            form.appendChild(transactionIdInput);
                            
                            const authCodeInput = document.createElement('input');
                            authCodeInput.type = 'hidden';
                            authCodeInput.name = 'authCode';
                            authCodeInput.value = '123456';
                            form.appendChild(authCodeInput);
                            
                            document.body.appendChild(form);
                            form.submit();
                        }
                        
                        function fail() {
                            const form = document.createElement('form');
                            form.method = 'POST';
                            form.action = '/api/v1/payments/3d-callback/fail';
                            
                            const orderIdInput = document.createElement('input');
                            orderIdInput.type = 'hidden';
                            orderIdInput.name = 'orderId';
                            orderIdInput.value = '%s';
                            form.appendChild(orderIdInput);
                            
                            const errorInput = document.createElement('input');
                            errorInput.type = 'hidden';
                            errorInput.name = 'errorMessage';
                            errorInput.value = 'User cancelled 3D Secure authentication';
                            form.appendChild(errorInput);
                            
                            document.body.appendChild(form);
                            form.submit();
                        }
                    </script>
                </div>
            </body>
            </html>
            """.formatted(orderId, orderId, orderId);
            
        return ResponseEntity.ok(html);
    }
}
