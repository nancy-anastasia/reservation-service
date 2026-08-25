package com.nancyk.reservation.common.exception;

public class ResourceInactiveException extends RuntimeException {

    public ResourceInactiveException(Long resourceId) {
        super("Resource is inactive: " + resourceId);
    }
}