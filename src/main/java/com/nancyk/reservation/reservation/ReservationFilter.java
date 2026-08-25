package com.nancyk.reservation.reservation;

import java.time.Instant;

public record ReservationFilter(
        Long resourceId,
        ReservationStatus status,
        String reservedBy,
        Instant from,
        Instant to
) {
}