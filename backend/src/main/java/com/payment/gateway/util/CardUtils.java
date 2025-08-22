package com.payment.gateway.util;

import org.springframework.stereotype.Component;

/**
 * Card utility methods for PCI DSS compliance and card processing
 * Tüm card-related utility metodları tek yerde toplanmıştır
 */
@Component
public class CardUtils {
    
    /**
     * Kart numarasını maskele (PCI DSS compliance için)
     * Format: BIN (6 digits) + masked middle + last 4 digits
     * Example: 4111111111111111 -> 411111******1111
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return cardNumber;
        }
        
        // BIN (first 6 digits) + masked middle + last 4 digits format
        String bin = cardNumber.substring(0, 6);
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        int middleLength = cardNumber.length() - 10; // Total - 6 (BIN) - 4 (last four)
        String maskedMiddle = "*".repeat(middleLength);
        
        return bin + maskedMiddle + lastFour;
    }
    
    /**
     * Kart numarasından BIN (Bank Identification Number) çıkar
     */
    public static String extractCardBin(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return null;
        }
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");
        return cleanCardNumber.length() >= 6 ? cleanCardNumber.substring(0, 6) : null;
    }
    
    /**
     * Kart numarasından son 4 haneyi çıkar
     */
    public static String extractCardLastFour(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return null;
        }
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");
        return cleanCardNumber.length() >= 4 ? cleanCardNumber.substring(cleanCardNumber.length() - 4) : null;
    }
    
    /**
     * Kart markasını tespit et
     */
    public static String detectCardBrand(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "UNKNOWN";
        }
        
        // Remove any non-digit characters
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");
        
        if (cleanCardNumber.length() < 4) {
            return "UNKNOWN";
        }
        
        // Get the first few digits to determine the brand
        String prefix = cleanCardNumber.substring(0, Math.min(6, cleanCardNumber.length()));
        int firstDigit = Integer.parseInt(prefix.substring(0, 1));
        int firstTwoDigits = Integer.parseInt(prefix.substring(0, 2));
        int firstThreeDigits = prefix.length() >= 3 ? Integer.parseInt(prefix.substring(0, 3)) : 0;
        int firstFourDigits = prefix.length() >= 4 ? Integer.parseInt(prefix.substring(0, 4)) : 0;
        
        // Visa: starts with 4
        if (firstDigit == 4) {
            return "VISA";
        }
        
        // Mastercard: 51-55, 2221-2720
        if (firstTwoDigits >= 51 && firstTwoDigits <= 55) {
            return "MASTERCARD";
        }
        if (firstFourDigits >= 2221 && firstFourDigits <= 2720) {
            return "MASTERCARD";
        }
        
        // American Express: 34, 37
        if (firstTwoDigits == 34 || firstTwoDigits == 37) {
            return "AMEX";
        }
        
        // Discover: 6011, 622126-622925, 644-649, 65
        if (firstFourDigits == 6011 || firstTwoDigits == 65) {
            return "DISCOVER";
        }
        if (firstThreeDigits >= 644 && firstThreeDigits <= 649) {
            return "DISCOVER";
        }
        if (firstFourDigits >= 622126 && firstFourDigits <= 622925) {
            return "DISCOVER";
        }
        
        // Diners Club: 300-305, 36, 38
        if (firstThreeDigits >= 300 && firstThreeDigits <= 305) {
            return "DINERS";
        }
        if (firstTwoDigits == 36 || firstTwoDigits == 38) {
            return "DINERS";
        }
        
        // JCB: 3528-3589
        if (firstFourDigits >= 3528 && firstFourDigits <= 3589) {
            return "JCB";
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Kart numarasının geçerli olup olmadığını kontrol et (Luhn algorithm)
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove all non-digit characters
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");
        
        // Check if length is valid (13-19 digits)
        if (cleanCardNumber.length() < 13 || cleanCardNumber.length() > 19) {
            return false;
        }
        
        // Luhn algorithm implementation
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cleanCardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cleanCardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
    
    /**
     * Kart numarasının formatını temizle (sadece rakamları al)
     */
    public static String cleanCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        return cardNumber.replaceAll("\\D", "");
    }
    
    /**
     * Kart numarasının uzunluğunu kontrol et
     */
    public static boolean isValidCardLength(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }
        String cleanCardNumber = cleanCardNumber(cardNumber);
        return cleanCardNumber.length() >= 13 && cleanCardNumber.length() <= 19;
    }
    
    /**
     * Kart numarasından prefix oluştur (ilk 6 + son 4 hane)
     * Privacy için masked format kullanır
     */
    public static String getCardPrefix(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return "UNKNOWN";
        }
        // Use first 6 digits + last 4 for identification while maintaining privacy
        String first6 = cardNumber.substring(0, 6);
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return first6 + "****" + last4;
    }
}
