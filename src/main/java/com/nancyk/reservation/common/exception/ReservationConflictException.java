package com.nancyk.reservation.common.exception;

public class ReservationConflictException extends RuntimeException {

    public ReservationConflictException(Long resourceId) {
        super("Resource already has an overlapping reservation: " + resourceId);
    }
}