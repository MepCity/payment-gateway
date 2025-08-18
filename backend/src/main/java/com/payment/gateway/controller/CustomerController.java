package com.payment.gateway.controller;

import com.payment.gateway.dto.CustomerRequest;
import com.payment.gateway.dto.CustomerResponse;
import com.payment.gateway.model.Customer;
import com.payment.gateway.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    
    private final CustomerService customerService;
    
    // POST - Create new customer
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        log.info("Creating new customer: {} {}", request.getFirstName(), request.getLastName());
        
        CustomerResponse response = customerService.createCustomer(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // GET - Get customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        log.info("Retrieving customer with ID: {}", id);
        
        CustomerResponse response = customerService.getCustomerById(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get customer by customer ID
    @GetMapping("/customer-id/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerByCustomerId(@PathVariable String customerId) {
        log.info("Retrieving customer with customer ID: {}", customerId);
        
        CustomerResponse response = customerService.getCustomerByCustomerId(customerId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get customer by email
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        log.info("Retrieving customer with email: {}", email);
        
        CustomerResponse response = customerService.getCustomerByEmail(email);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get all customers
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        log.info("Retrieving all customers");
        
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }
    
    // GET - Get customers by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CustomerResponse>> getCustomersByStatus(@PathVariable Customer.CustomerStatus status) {
        log.info("Retrieving customers with status: {}", status);
        
        List<CustomerResponse> customers = customerService.getCustomersByStatus(status);
        return ResponseEntity.ok(customers);
    }
    
    // GET - Search customers by name
    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchCustomersByName(@RequestParam String name) {
        log.info("Searching customers by name: {}", name);
        
        List<CustomerResponse> customers = customerService.searchCustomersByName(name);
        return ResponseEntity.ok(customers);
    }
    
    // POST - Update customer
    @PostMapping("/{id}/update")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        log.info("Updating customer with ID: {}", id);
        
        CustomerResponse response = customerService.updateCustomer(id, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // PUT - Update customer status
    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerResponse> updateCustomerStatus(
            @PathVariable Long id, 
            @RequestParam Customer.CustomerStatus status) {
        log.info("Updating customer status to {} for ID: {}", status, id);
        
        CustomerResponse response = customerService.updateCustomerStatus(id, status);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // DEL - Delete customer
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomerResponse> deleteCustomer(@PathVariable Long id) {
        log.info("Deleting customer with ID: {}", id);
        
        CustomerResponse response = customerService.deleteCustomer(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    

}
