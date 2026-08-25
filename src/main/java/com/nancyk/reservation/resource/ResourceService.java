package com.nancyk.reservation.resource;

import com.nancyk.reservation.common.exception.ResourceAlreadyInactiveException;
import com.nancyk.reservation.common.exception.ResourceNotFoundException;
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

    public List<ResourceResponse> findAll(ResourceFilter filter) {
        var specification =
                ResourceSpecifications.hasType(filter.type())
                        .and(ResourceSpecifications.hasActive(filter.active()))
                        .and(ResourceSpecifications.nameContains(filter.name()));

        return resourceRepository.findAll(specification)
                .stream()
                .map(ResourceResponse::from)
                .toList();
    }

    @Transactional
    public ResourceResponse deactivate(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(resourceId)
                );

        if (resource.isInactive()) {
            throw new ResourceAlreadyInactiveException(resourceId);
        }

        resource.deactivate();

        Resource saved = resourceRepository.saveAndFlush(resource);

        return ResourceResponse.from(saved);
    }
}