package com.payment.gateway.dto;

import com.payment.gateway.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phone;
    
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;
    
    @NotNull(message = "Address is required")
    private AddressRequest address;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressRequest {
        @NotBlank(message = "Street is required")
        @Size(min = 5, max = 100, message = "Street must be between 5 and 100 characters")
        private String street;
        
        @NotBlank(message = "City is required")
        @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
        private String city;
        
        @NotBlank(message = "Postal code is required")
        @Pattern(regexp = "^[0-9A-Za-z\\s-]{3,10}$", message = "Postal code must be 3-10 characters")
        private String postalCode;
        
        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters")
        private String country;
    }
}
