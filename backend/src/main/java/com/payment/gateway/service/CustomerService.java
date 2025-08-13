package com.payment.gateway.service;

import com.payment.gateway.dto.CustomerRequest;
import com.payment.gateway.dto.CustomerResponse;
import com.payment.gateway.model.Customer;
import com.payment.gateway.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.payment.gateway.service.AuditService;
import com.payment.gateway.model.AuditLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final AuditService auditService;
    
    public CustomerResponse createCustomer(CustomerRequest request) {
        try {
            // Check if email already exists
            if (customerRepository.existsByEmail(request.getEmail())) {
                return createErrorResponse("Customer with email " + request.getEmail() + " already exists");
            }
            
            // Generate unique customer ID
            String customerId = generateCustomerId();
            
            // Create customer entity
            Customer customer = new Customer();
            customer.setCustomerId(customerId);
            customer.setFirstName(request.getFirstName());
            customer.setLastName(request.getLastName());
            customer.setEmail(request.getEmail());
            customer.setPhoneNumber(request.getPhone());
            customer.setAddress(request.getAddress().getStreet());
            customer.setCity(request.getAddress().getCity());
            customer.setCountry(request.getAddress().getCountry());
            customer.setPostalCode(request.getAddress().getPostalCode());
            customer.setStatus(Customer.CustomerStatus.ACTIVE);
            customer.setNotes(request.getNotes());
            
            // Save customer
            Customer savedCustomer = customerRepository.save(customer);
            
            // Audit logging
            auditService.createEvent()
                .eventType("CUSTOMER_CREATED")
                .severity(AuditLog.Severity.LOW)
                .actor("system")
                .action("CREATE")
                .resourceType("CUSTOMER")
                .resourceId(customerId)
                .newValues(savedCustomer)
                .complianceTag("GDPR")
                .log();
            
            log.info("Customer created successfully with ID: {}", customerId);
            
            return createCustomerResponse(savedCustomer, "Customer created successfully", true);
            
        } catch (Exception e) {
            log.error("Error creating customer: {}", e.getMessage());
            return createErrorResponse("Failed to create customer: " + e.getMessage());
        }
    }
    
    public CustomerResponse getCustomerById(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return createCustomerResponse(customer.get(), "Customer retrieved successfully", true);
        } else {
            return createErrorResponse("Customer not found with ID: " + id);
        }
    }
    
    public CustomerResponse getCustomerByCustomerId(String customerId) {
        Optional<Customer> customer = customerRepository.findByCustomerId(customerId);
        if (customer.isPresent()) {
            return createCustomerResponse(customer.get(), "Customer retrieved successfully", true);
        } else {
            return createErrorResponse("Customer not found with customer ID: " + customerId);
        }
    }
    
    public CustomerResponse getCustomerByEmail(String email) {
        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isPresent()) {
            return createCustomerResponse(customer.get(), "Customer retrieved successfully", true);
        } else {
            return createErrorResponse("Customer not found with email: " + email);
        }
    }
    
    public List<CustomerResponse> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(customer -> createCustomerResponse(customer, null, true))
                .collect(Collectors.toList());
    }
    
    public List<CustomerResponse> getCustomersByStatus(Customer.CustomerStatus status) {
        List<Customer> customers = customerRepository.findByStatus(status);
        return customers.stream()
                .map(customer -> createCustomerResponse(customer, null, true))
                .collect(Collectors.toList());
    }
    
    public List<CustomerResponse> getCustomersByCity(String city) {
        List<Customer> customers = customerRepository.findByCity(city);
        return customers.stream()
                .map(customer -> createCustomerResponse(customer, null, true))
                .collect(Collectors.toList());
    }
    
    public List<CustomerResponse> getCustomersByCountry(String country) {
        List<Customer> customers = customerRepository.findByCountry(country);
        return customers.stream()
                .map(customer -> createCustomerResponse(customer, null, true))
                .collect(Collectors.toList());
    }
    
    public List<CustomerResponse> searchCustomersByName(String name) {
        List<Customer> customers = customerRepository.findByNameContaining(name);
        return customers.stream()
                .map(customer -> createCustomerResponse(customer, null, true))
                .collect(Collectors.toList());
    }
    
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            
            // Check if email is being changed and if it already exists
            if (!customer.getEmail().equals(request.getEmail()) && 
                customerRepository.existsByEmail(request.getEmail())) {
                return createErrorResponse("Customer with email " + request.getEmail() + " already exists");
            }
            
            // Update customer fields
            customer.setFirstName(request.getFirstName());
            customer.setLastName(request.getLastName());
            customer.setEmail(request.getEmail());
            customer.setPhoneNumber(request.getPhone());
            customer.setAddress(request.getAddress().getStreet());
            customer.setCity(request.getAddress().getCity());
            customer.setCountry(request.getAddress().getCountry());
            customer.setPostalCode(request.getAddress().getPostalCode());
            customer.setNotes(request.getNotes());
            
            Customer updatedCustomer = customerRepository.save(customer);
            
            // Audit logging
            auditService.createEvent()
                .eventType("CUSTOMER_UPDATED")
                .severity(AuditLog.Severity.LOW)
                .actor("system")
                .action("UPDATE")
                .resourceType("CUSTOMER")
                .resourceId(id.toString())
                .oldValues(customerOpt.get())
                .newValues(updatedCustomer)
                .complianceTag("GDPR")
                .log();
            
            log.info("Customer updated successfully with ID: {}", id);
            return createCustomerResponse(updatedCustomer, "Customer updated successfully", true);
        } else {
            return createErrorResponse("Customer not found with ID: " + id);
        }
    }
    
    public CustomerResponse updateCustomerStatus(Long id, Customer.CustomerStatus newStatus) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setStatus(newStatus);
            Customer updatedCustomer = customerRepository.save(customer);
            
            // Audit logging
            auditService.createEvent()
                .eventType("CUSTOMER_STATUS_UPDATED")
                .severity(AuditLog.Severity.MEDIUM)
                .actor("system")
                .action("UPDATE_STATUS")
                .resourceType("CUSTOMER")
                .resourceId(id.toString())
                .oldValues(customerOpt.get())
                .newValues(updatedCustomer)
                .additionalData("newStatus", newStatus.name())
                .complianceTag("GDPR")
                .log();
            
            log.info("Customer status updated to {} for ID: {}", newStatus, id);
            return createCustomerResponse(updatedCustomer, "Customer status updated successfully", true);
        } else {
            return createErrorResponse("Customer not found with ID: " + id);
        }
    }
    
    public CustomerResponse deleteCustomer(Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            
            // Soft delete - set status to DELETED
            customer.setStatus(Customer.CustomerStatus.DELETED);
            Customer updatedCustomer = customerRepository.save(customer);
            
            // Audit logging
            auditService.createEvent()
                .eventType("CUSTOMER_DELETED")
                .severity(AuditLog.Severity.MEDIUM)
                .actor("system")
                .action("DELETE")
                .resourceType("CUSTOMER")
                .resourceId(id.toString())
                .oldValues(customerOpt.get())
                .newValues(updatedCustomer)
                .complianceTag("GDPR")
                .log();
            
            log.info("Customer deleted successfully with ID: {}", id);
            return createCustomerResponse(updatedCustomer, "Customer deleted successfully", true);
        } else {
            return createErrorResponse("Customer not found with ID: " + id);
        }
    }
    
    private String generateCustomerId() {
        return "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private CustomerResponse createCustomerResponse(Customer customer, String message, boolean success) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setCustomerId(customer.getCustomerId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setAddress(customer.getAddress());
        response.setCity(customer.getCity());
        response.setCountry(customer.getCountry());
        response.setPostalCode(customer.getPostalCode());
        response.setStatus(customer.getStatus());
        response.setNotes(customer.getNotes());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        response.setMessage(message);
        response.setSuccess(success);
        return response;
    }
    
    private CustomerResponse createErrorResponse(String errorMessage) {
        CustomerResponse response = new CustomerResponse();
        response.setMessage(errorMessage);
        response.setSuccess(false);
        return response;
    }
}
