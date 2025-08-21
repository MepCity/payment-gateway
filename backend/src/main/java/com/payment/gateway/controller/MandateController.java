package com.payment.gateway.controller;

import com.payment.gateway.dto.MandateRequest;
import com.payment.gateway.dto.MandateResponse;
import com.payment.gateway.model.Mandate;
import com.payment.gateway.service.MandateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/mandates")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MandateController {
    
    private final MandateService mandateService;
    
    // POST - Create new mandate
    @PostMapping
    public ResponseEntity<MandateResponse> createMandate(@Valid @RequestBody MandateRequest request) {
        log.info("Creating new mandate for customer: {}, merchant: {}", 
                request.getCustomerId(), request.getMerchantId());
        
        MandateResponse response = mandateService.createMandate(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // GET - Get mandate by ID
    @GetMapping("/{id}")
    public ResponseEntity<MandateResponse> getMandateById(@PathVariable Long id) {
        log.info("Retrieving mandate with ID: {}", id);
        
        MandateResponse response = mandateService.getMandateById(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get mandate by mandate ID
    @GetMapping("/mandate-id/{mandateId}")
    public ResponseEntity<MandateResponse> getMandateByMandateId(@PathVariable String mandateId) {
        log.info("Retrieving mandate with mandate ID: {}", mandateId);
        
        MandateResponse response = mandateService.getMandateByMandateId(mandateId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get all mandates
    @GetMapping
    public ResponseEntity<List<MandateResponse>> getAllMandates() {
        log.info("Retrieving all mandates");
        
        List<MandateResponse> mandates = mandateService.getAllMandates();
        return ResponseEntity.ok(mandates);
    }
    
    // GET - Get mandates by customer ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<MandateResponse>> getMandatesByCustomerId(@PathVariable String customerId) {
        log.info("Retrieving mandates for customer: {}", customerId);
        
        List<MandateResponse> mandates = mandateService.getMandatesByCustomerId(customerId);
        return ResponseEntity.ok(mandates);
    }
    
    // GET - Get mandates by merchant ID
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<MandateResponse>> getMandatesByMerchantId(@PathVariable String merchantId) {
        log.info("Retrieving mandates for merchant: {}", merchantId);
        
        List<MandateResponse> mandates = mandateService.getMandatesByMerchantId(merchantId);
        return ResponseEntity.ok(mandates);
    }
    
    // GET - Get mandates by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MandateResponse>> getMandatesByStatus(@PathVariable Mandate.MandateStatus status) {
        log.info("Retrieving mandates with status: {}", status);
        
        List<MandateResponse> mandates = mandateService.getMandatesByStatus(status);
        return ResponseEntity.ok(mandates);
    }
    
    // GET - Get active customer mandates
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<List<MandateResponse>> getActiveCustomerMandates(@PathVariable String customerId) {
        log.info("Retrieving active mandates for customer: {}", customerId);
        
        List<MandateResponse> mandates = mandateService.getActiveCustomerMandates(customerId);
        return ResponseEntity.ok(mandates);
    }
    
    // POST - Revoke mandate
    @PostMapping("/{id}/revoke")
    public ResponseEntity<MandateResponse> revokeMandate(@PathVariable Long id) {
        log.info("Revoking mandate with ID: {}", id);
        
        MandateResponse response = mandateService.revokeMandate(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // PUT - Update mandate status
    @PutMapping("/{id}/status")
    public ResponseEntity<MandateResponse> updateMandateStatus(
            @PathVariable Long id, 
            @RequestParam Mandate.MandateStatus status) {
        log.info("Updating mandate status to {} for ID: {}", status, id);
        
        MandateResponse response = mandateService.updateMandateStatus(id, status);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    

}