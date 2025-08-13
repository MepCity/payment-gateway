package com.payment.gateway.repository;

import com.payment.gateway.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find by event type
    List<AuditLog> findByEventTypeOrderByTimestampDesc(String eventType);
    
    // Find by actor
    List<AuditLog> findByActorOrderByTimestampDesc(String actor);
    
    // Find by resource
    List<AuditLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(
        String resourceType, String resourceId);
    
    // Find by severity
    List<AuditLog> findBySeverityOrderByTimestampDesc(AuditLog.Severity severity);
    
    // Find by time range
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    List<AuditLog> findByTimeRange(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime);
    
    // Find by multiple criteria with pagination
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:eventType IS NULL OR a.eventType = :eventType) AND " +
           "(:actor IS NULL OR a.actor = :actor) AND " +
           "(:severity IS NULL OR a.severity = :severity) AND " +
           "(:resourceType IS NULL OR a.resourceType = :resourceType) AND " +
           "a.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByFilters(
        @Param("eventType") String eventType,
        @Param("actor") String actor,
        @Param("severity") AuditLog.Severity severity,
        @Param("resourceType") String resourceType,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable);
    
    // Find security events (HIGH and CRITICAL severity)
    @Query("SELECT a FROM AuditLog a WHERE a.severity IN ('HIGH', 'CRITICAL') " +
           "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findSecurityEvents(@Param("since") LocalDateTime since);
    
    // Find expired records for cleanup
    @Query("SELECT a FROM AuditLog a WHERE a.retentionUntil < :now")
    List<AuditLog> findExpiredRecords(@Param("now") LocalDateTime now);
    
    // Count events by type in time range
    @Query("SELECT a.eventType, COUNT(a) FROM AuditLog a WHERE " +
           "a.timestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY a.eventType ORDER BY COUNT(a) DESC")
    List<Object[]> countEventsByType(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime);
    
    // Count events by actor in time range
    @Query("SELECT a.actor, COUNT(a) FROM AuditLog a WHERE " +
           "a.timestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY a.actor ORDER BY COUNT(a) DESC")
    List<Object[]> countEventsByActor(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime);
}
