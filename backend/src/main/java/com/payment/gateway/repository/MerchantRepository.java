package com.payment.gateway.repository;

import com.payment.gateway.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    
    /**
     * Merchant ID ile merchant bul
     */
    Optional<Merchant> findByMerchantId(String merchantId);
    
    /**
     * API key ile merchant bul
     */
    Optional<Merchant> findByApiKey(String apiKey);
    
    /**
     * Email ile merchant bul
     */
    Optional<Merchant> findByEmail(String email);
    
    /**
     * Status'e göre merchant'ları listele
     */
    List<Merchant> findByStatus(Merchant.MerchantStatus status);
    
    /**
     * Aktif merchant'ları listele
     */
    @Query("SELECT m FROM Merchant m WHERE m.status = 'ACTIVE'")
    List<Merchant> findActiveMerchants();
    
    /**
     * Merchant ID ve API key eşleşmesi kontrol et
     */
    @Query("SELECT m FROM Merchant m WHERE m.merchantId = :merchantId AND m.apiKey = :apiKey AND m.status = 'ACTIVE'")
    Optional<Merchant> findByMerchantIdAndApiKey(@Param("merchantId") String merchantId, @Param("apiKey") String apiKey);
    
    /**
     * API key'in varlığını kontrol et
     */
    boolean existsByApiKey(String apiKey);
    
    /**
     * Merchant ID'nin varlığını kontrol et
     */
    boolean existsByMerchantId(String merchantId);
}
