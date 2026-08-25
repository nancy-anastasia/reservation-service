package com.nancyk.reservation.common.exception;

public class ResourceAlreadyInactiveException extends RuntimeException {

    public ResourceAlreadyInactiveException(Long resourceId) {
        super("Resource is already inactive: " + resourceId);
    }
}