package com.payment.gateway.repository;

import com.payment.gateway.model.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MandateRepository extends JpaRepository<Mandate, Long> {
    
    Optional<Mandate> findByMandateId(String mandateId);
    
    List<Mandate> findByCustomerId(String customerId);
    
    List<Mandate> findByMerchantId(String merchantId);
    
    List<Mandate> findByStatus(Mandate.MandateStatus status);
    
    List<Mandate> findByCustomerIdAndStatus(String customerId, Mandate.MandateStatus status);
    
    List<Mandate> findByMerchantIdAndStatus(String merchantId, Mandate.MandateStatus status);
    
    @Query("SELECT m FROM Mandate m WHERE m.startDate <= :date AND (m.endDate IS NULL OR m.endDate >= :date)")
    List<Mandate> findActiveMandatesOnDate(@Param("date") LocalDateTime date);
    
    @Query("SELECT m FROM Mandate m WHERE m.customerId = :customerId AND m.startDate <= :date AND (m.endDate IS NULL OR m.endDate >= :date)")
    List<Mandate> findActiveCustomerMandatesOnDate(@Param("customerId") String customerId, @Param("date") LocalDateTime date);
    
    @Query("SELECT m FROM Mandate m WHERE m.merchantId = :merchantId AND m.startDate <= :date AND (m.endDate IS NULL OR m.endDate >= :date)")
    List<Mandate> findActiveMerchantMandatesOnDate(@Param("merchantId") String merchantId, @Param("date") LocalDateTime date);
    
    boolean existsByMandateId(String mandateId);
    
    boolean existsByGatewayMandateId(String gatewayMandateId);
}
