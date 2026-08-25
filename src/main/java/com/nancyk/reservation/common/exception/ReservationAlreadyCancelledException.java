package com.nancyk.reservation.common.exception;

public class ReservationAlreadyCancelledException extends RuntimeException {

    public ReservationAlreadyCancelledException(Long reservationId) {
        super("Reservation is already cancelled: " + reservationId);
    }
}