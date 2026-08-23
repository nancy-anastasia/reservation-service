package com.nancyk.reservation.resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public ResourceResponse create(CreateResourceRequest request) {
        Resource resource = new Resource(
                request.name(),
                request.description(),
                request.type()
        );

        Resource savedResource = resourceRepository.save(resource);

        return ResourceResponse.from(savedResource);
    }

    public List<ResourceResponse> findAll() {
        return resourceRepository.findAll()
                .stream()
                .map(ResourceResponse::from)
                .toList();
    }
}