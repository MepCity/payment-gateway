package com.payment.gateway.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.payment.gateway.util.CardUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Abstract base class for bank adapters
 * Ortak HTTP işlemleri ve format dönüşümleri
 */
@Slf4j
public abstract class AbstractBankAdapter implements BankAdapter {
    
    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;
    protected final XmlMapper xmlMapper;
    
    public AbstractBankAdapter() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.xmlMapper = new XmlMapper();
    }
    
    /**
     * JSON formatında POST isteği gönder
     */
    protected ResponseEntity<String> sendJsonRequest(String url, Object requestBody, HttpHeaders headers) {
        try {
            if (headers == null) {
                headers = new HttpHeaders();
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            log.debug("Sending JSON request to {}: {}", url, jsonBody);
            
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
        } catch (Exception e) {
            log.error("Error sending JSON request to {}: {}", url, e.getMessage());
            throw new RuntimeException("Failed to send JSON request", e);
        }
    }
    
    /**
     * XML formatında POST isteği gönder
     */
    protected ResponseEntity<String> sendXmlRequest(String url, Object requestBody, HttpHeaders headers) {
        try {
            if (headers == null) {
                headers = new HttpHeaders();
            }
            headers.setContentType(MediaType.APPLICATION_XML);
            
            String xmlBody = xmlMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(xmlBody, headers);
            
            log.debug("Sending XML request to {}: {}", url, xmlBody);
            
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
        } catch (Exception e) {
            log.error("Error sending XML request to {}: {}", url, e.getMessage());
            throw new RuntimeException("Failed to send XML request", e);
        }
    }
    
    /**
     * Form data formatında POST isteği gönder
     */
    protected ResponseEntity<String> sendFormDataRequest(String url, Map<String, String> formData, HttpHeaders headers) {
        try {
            if (headers == null) {
                headers = new HttpHeaders();
            }
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            formData.forEach(form::add);
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
            
            log.debug("Sending form data request to {}: {}", url, formData);
            
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
        } catch (Exception e) {
            log.error("Error sending form data request to {}: {}", url, e.getMessage());
            throw new RuntimeException("Failed to send form data request", e);
        }
    }
    
    /**
     * Response'u JSON'dan objeye dönüştür
     */
    protected <T> T parseJsonResponse(String jsonResponse, Class<T> responseClass) {
        try {
            return objectMapper.readValue(jsonResponse, responseClass);
        } catch (Exception e) {
            log.error("Error parsing JSON response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }
    
    /**
     * Response'u XML'den objeye dönüştür
     */
    protected <T> T parseXmlResponse(String xmlResponse, Class<T> responseClass) {
        try {
            return xmlMapper.readValue(xmlResponse, responseClass);
        } catch (Exception e) {
            log.error("Error parsing XML response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse XML response", e);
        }
    }
    
    /**
     * Kart numarasının BIN'ini al
     */
    protected String getCardBin(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return null;
        }
        return cardNumber.substring(0, 6);
    }

    
    /**
     * HTTP Basic Auth header oluştur
     */
    protected HttpHeaders createBasicAuthHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + password;
        String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
    
    /**
     * Unique order ID oluştur
     */
    protected String generateOrderId() {
        return "ORD-" + System.currentTimeMillis();
    }
    
    /**
     * HTML form auto-submit oluştur
     */
    protected String createAutoSubmitForm(String actionUrl, Map<String, String> formFields) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<title>3D Secure Redirect</title>\n");
        html.append("</head>\n<body>\n");
        html.append("<form id='threeDForm' method='POST' action='").append(actionUrl).append("'>\n");
        
        formFields.forEach((key, value) -> {
            html.append("<input type='hidden' name='").append(key).append("' value='").append(value).append("' />\n");
        });
        
        html.append("</form>\n");
        html.append("<script>document.getElementById('threeDForm').submit();</script>\n");
        html.append("</body>\n</html>");
        
        return html.toString();
    }
}
