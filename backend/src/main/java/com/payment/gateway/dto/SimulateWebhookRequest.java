package com.payment.gateway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulateWebhookRequest {
    
    private String status; // SUCCESS, FAILED, PROCESSING, CANCELLED
    private String message;
    private String bankType; // GARANTI, ISBANK, AKBANK
    private String gatewayRefundId;
    
}
