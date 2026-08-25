package com.nancyk.reservation.resource;

public record ResourceFilter(
        ResourceType type,
        Boolean active,
        String name
) {
}
