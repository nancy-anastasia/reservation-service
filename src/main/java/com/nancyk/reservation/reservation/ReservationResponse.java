package com.nancyk.reservation.reservation;

import java.time.Instant;

public record ReservationResponse(
        Long id,
        Long resourceId,
        String reservedBy,
        Instant startsAt,
        Instant endsAt,
        ReservationStatus status,
        Instant createdAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getResource().getId(),
                reservation.getReservedBy(),
                reservation.getStartsAt(),
                reservation.getEndsAt(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}