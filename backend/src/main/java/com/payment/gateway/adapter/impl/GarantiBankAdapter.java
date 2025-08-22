package com.payment.gateway.adapter.impl;

import com.payment.gateway.adapter.AbstractBankAdapter;
import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.dto.ThreeDSecureRequest;
import com.payment.gateway.dto.ThreeDSecureResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Garanti BBVA 3D Secure Adapter
 * Garanti BBVA XML formatını kullanır
 */
@Component
@Slf4j
public class GarantiBankAdapter extends AbstractBankAdapter {
    
    @Value("${app.bank.garanti.3d.enabled:true}")
    private boolean enabled;
    
    @Value("${app.bank.garanti.3d.endpoint:https://sanalposprov.garanti.com.tr/VPosService/v3/Vpos}")
    private String threeDEndpoint;
    
    @Value("${app.bank.garanti.test.merchant-id:000000000111111}")
    private String merchantId;
    
    @Value("${app.bank.garanti.test.terminal-id:00111111}")
    private String terminalId;
    
    @Value("${app.bank.garanti.test.username:PROVAUT}")
    private String username;
    
    @Value("${app.bank.garanti.test.password:123456}")
    private String password;
    
    // Garanti BBVA BIN'leri
    private static final String[] GARANTI_BINS = {
        "482494", // Test kartları
        "540061", // Garanti Bonus
        "454360", // Garanti Miles&Smiles
        "518134", // Garanti Flexi
        "526717", // Garanti Maximum
        "554960"  // Garanti Advantage
    };
    
    @Override
    public String getBankName() {
        return "GARANTI_BBVA";
    }
    
    @Override
    public RequestFormat getRequestFormat() {
        return RequestFormat.XML;
    }
    
    @Override
    public ResponseFormat getResponseFormat() {
        return ResponseFormat.XML;
    }
    
    @Override
    public boolean supportsBin(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return false;
        }
        
        String bin = cardNumber.substring(0, 6);
        for (String garantiBin : GARANTI_BINS) {
            if (bin.startsWith(garantiBin)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ThreeDSecureResponse initiate3DSecure(PaymentRequest paymentRequest, ThreeDSecureRequest threeDRequest) {
        log.info("Initiating 3D Secure with Garanti BBVA for order: {}", threeDRequest.getOrderId());
        
        try {
            // Garanti XML request oluştur
            String xmlRequest = buildGarantiXmlRequest(threeDRequest);
            
            // HTTP headers oluştur
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/xml; charset=utf-8");
            headers.set("SOAPAction", "\"\"");
            
            // Garanti'ye XML request gönder
            ResponseEntity<String> response = sendXmlRequest(threeDEndpoint, xmlRequest, headers);
            
            // Response'u parse et
            return parseGarantiResponse(response.getBody(), threeDRequest);
            
        } catch (Exception e) {
            log.error("Error initiating 3D Secure with Garanti: {}", e.getMessage());
            return ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("Garanti 3D Secure başlatma hatası: " + e.getMessage())
                    .bankName(getBankName())
                    .build();
        }
    }
    
    @Override
    public ThreeDSecureResponse handle3DCallback(String callbackData) {
        log.info("Handling 3D Secure callback from Garanti BBVA");
        
        try {
            // Garanti callback data'sını parse et (genellikle XML veya form data)
            Map<String, String> callbackParams = parseCallbackData(callbackData);
            
            String mdStatus = callbackParams.get("mdstatus");
            String orderId = callbackParams.get("orderid");
            
            ThreeDSecureResponse.ThreeDStatus status;
            boolean success = false;
            String message = "";
            
            // Garanti MD Status kodları
            switch (mdStatus) {
                case "1":
                    status = ThreeDSecureResponse.ThreeDStatus.AUTHENTICATED;
                    success = true;
                    message = "3D Secure authentication successful";
                    break;
                case "2":
                case "3":
                case "4":
                    status = ThreeDSecureResponse.ThreeDStatus.AUTHENTICATED;
                    success = true;
                    message = "3D Secure authentication successful (partial)";
                    break;
                case "5":
                    status = ThreeDSecureResponse.ThreeDStatus.NOT_ENROLLED;
                    message = "Card not enrolled for 3D Secure";
                    break;
                case "6":
                    status = ThreeDSecureResponse.ThreeDStatus.NOT_ENROLLED;
                    message = "3D Secure not available";
                    break;
                case "7":
                    status = ThreeDSecureResponse.ThreeDStatus.ERROR;
                    message = "System error";
                    break;
                case "8":
                    status = ThreeDSecureResponse.ThreeDStatus.FAILED;
                    message = "Unknown card";
                    break;
                default:
                    status = ThreeDSecureResponse.ThreeDStatus.FAILED;
                    message = "Authentication failed";
            }
            
            return ThreeDSecureResponse.builder()
                    .success(success)
                    .status(status)
                    .message(message)
                    .orderId(orderId)
                    .mdStatus(mdStatus)
                    .cavv(callbackParams.get("cavv"))
                    .eci(callbackParams.get("eci"))
                    .xid(callbackParams.get("xid"))
                    .bankName(getBankName())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error handling Garanti 3D callback: {}", e.getMessage());
            return ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("Callback işleme hatası: " + e.getMessage())
                    .bankName(getBankName())
                    .build();
        }
    }
    
    @Override
    public boolean isTestMode() {
        return true; // Test ortamı
    }
    
    @Override
    public boolean isConfigured() {
        return enabled && merchantId != null && terminalId != null && username != null && password != null;
    }
    
    /**
     * Garanti XML request oluştur
     */
    private String buildGarantiXmlRequest(ThreeDSecureRequest request) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        xml.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
        xml.append("xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
        xml.append("<soap:Body>\n");
        xml.append("<GVPSRequest xmlns=\"https://www.garanti.com.tr/\">\n");
        xml.append("<Mode>TEST</Mode>\n"); // Test modu
        xml.append("<Version>v0.01</Version>\n");
        xml.append("<ChannelCode></ChannelCode>\n");
        xml.append("<Terminal>\n");
        xml.append("<ProvUserID>").append(username).append("</ProvUserID>\n");
        xml.append("<HashData>").append(generateGarantiHash(request)).append("</HashData>\n");
        xml.append("<UserID>").append(username).append("</UserID>\n");
        xml.append("<ID>").append(terminalId).append("</ID>\n");
        xml.append("<MerchantID>").append(merchantId).append("</MerchantID>\n");
        xml.append("</Terminal>\n");
        xml.append("<Customer>\n");
        xml.append("<IPAddress>").append(request.getCustomerIp()).append("</IPAddress>\n");
        xml.append("<EmailAddress>").append(request.getCustomerEmail()).append("</EmailAddress>\n");
        xml.append("</Customer>\n");
        xml.append("<Card>\n");
        xml.append("<Number>").append(request.getCardNumber()).append("</Number>\n");
        xml.append("<ExpireDate>").append(request.getExpiryMonth()).append(request.getExpiryYear()).append("</ExpireDate>\n");
        xml.append("<CVV2>").append(request.getCvv()).append("</CVV2>\n");
        xml.append("</Card>\n");
        xml.append("<Order>\n");
        xml.append("<OrderID>").append(request.getOrderId()).append("</OrderID>\n");
        xml.append("<GroupID></GroupID>\n");
        xml.append("</Order>\n");
        xml.append("<Transaction>\n");
        xml.append("<Type>sales</Type>\n");
        xml.append("<InstallmentCnt>").append(request.getInstallment() != null ? request.getInstallment() : "").append("</InstallmentCnt>\n");
        xml.append("<Amount>").append(request.getAmount().multiply(new java.math.BigDecimal(100)).intValue()).append("</Amount>\n");
        xml.append("<CurrencyCode>949</CurrencyCode>\n"); // TL
        xml.append("<CardholderPresentCode>0</CardholderPresentCode>\n");
        xml.append("<MotoInd>N</MotoInd>\n");
        xml.append("</Transaction>\n");
        xml.append("</GVPSRequest>\n");
        xml.append("</soap:Body>\n");
        xml.append("</soap:Envelope>");
        
        return xml.toString();
    }
    
    /**
     * Garanti hash oluştur (güvenlik için)
     */
    private String generateGarantiHash(ThreeDSecureRequest request) {
        // Garanti hash algoritması: SHA-1(TerminalID + OrderID + Amount + CurrencyCode + SuccessURL + ErrorURL + Type + InstallmentCount + StoreKey + HashPassword)
        String hashData = terminalId + request.getOrderId() + 
                         request.getAmount().multiply(new java.math.BigDecimal(100)).intValue() + 
                         "949" + request.getSuccessUrl() + request.getFailUrl() + 
                         "sales" + "" + password;
        
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(hashData.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            log.error("Error generating Garanti hash: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Garanti response parse et
     */
    private ThreeDSecureResponse parseGarantiResponse(String xmlResponse, ThreeDSecureRequest request) {
        // XML response'u parse et (basitleştirilmiş)
        if (xmlResponse.contains("Approved") || xmlResponse.contains("00")) {
            // 3D Secure sayfasına yönlendirme URL'i oluştur
            String redirectUrl = "https://sanalposprov.garanti.com.tr/servlet/gt3dengine?" +
                    "orderid=" + request.getOrderId() + 
                    "&merchantid=" + merchantId + 
                    "&terminalid=" + terminalId;
            
            return ThreeDSecureResponse.builder()
                    .success(true)
                    .status(ThreeDSecureResponse.ThreeDStatus.INITIATED)
                    .message("3D Secure başlatıldı, kullanıcı yönlendiriliyor")
                    .redirectUrl(redirectUrl)
                    .orderId(request.getOrderId())
                    .bankName(getBankName())
                    .build();
        } else {
            return ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("Garanti 3D Secure başlatma başarısız")
                    .bankName(getBankName())
                    .build();
        }
    }
    
    /**
     * Callback data parse et
     */
    private Map<String, String> parseCallbackData(String callbackData) {
        Map<String, String> params = new HashMap<>();
        
        // Form data veya query string parse et
        String[] pairs = callbackData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        
        return params;
    }
}