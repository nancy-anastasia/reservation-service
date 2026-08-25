package com.nancyk.reservation.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.resource.id = :resourceId
          AND r.status = :status
          AND r.startsAt < :requestedEnd
          AND r.endsAt > :requestedStart
        """)
    boolean existsOverlappingReservation(
            @Param("resourceId") Long resourceId,
            @Param("status") ReservationStatus status,
            @Param("requestedStart") Instant requestedStart,
            @Param("requestedEnd") Instant requestedEnd
    );
}