package com.nancyk.reservation.common.exception;

public class InvalidReservationPeriodException extends RuntimeException {

    public InvalidReservationPeriodException() {
        super("Reservation end time must be after start time");
    }
}