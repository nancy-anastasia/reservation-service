package com.nancyk.reservation.resource;

import org.springframework.stereotype.Component;

@Component
public class ResourceTestFactory {

    private final ResourceRepository resourceRepository;

    public ResourceTestFactory(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Resource createActiveResource() {
        return resourceRepository.save(
                new Resource(
                        "Conference Room A",
                        "Large conference room",
                        ResourceType.MEETING_ROOM
                )
        );
    }
}