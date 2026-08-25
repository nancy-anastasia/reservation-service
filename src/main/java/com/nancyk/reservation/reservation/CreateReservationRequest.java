package com.nancyk.reservation.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateReservationRequest(
        @NotNull
        Long resourceId,

        @NotBlank
        @Size(max = 150)
        String reservedBy,

        @NotNull
        Instant startsAt,

        @NotNull
        Instant endsAt
) {
}