package com.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDisputeResponse {
    
    private String responseType; // ACCEPT, DEFEND
    private String defenseEvidence;
    private List<String> supportingDocuments;
    private String additionalNotes;
    private String contactPhone;
    private String contactEmail;
}
