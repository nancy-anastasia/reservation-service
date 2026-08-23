package com.nancyk.reservation.resource;

import java.time.Instant;

public record ResourceResponse(
        Long id,
        String name,
        String description,
        ResourceType type,
        boolean active,
        Instant createdAt
) {
    public static ResourceResponse from(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getType(),
                resource.isActive(),
                resource.getCreatedAt()
        );
    }
}