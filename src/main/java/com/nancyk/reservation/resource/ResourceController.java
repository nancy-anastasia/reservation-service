package com.nancyk.reservation.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@Tag(
        name = "Resources",
        description = "Create, search, retrieve, and deactivate reservable resources"
)
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a resource",
            description = "Creates a new active reservable resource."
    )
    public ResourceResponse create(
            @Valid @RequestBody CreateResourceRequest request
    ) {
        return resourceService.create(request);
    }

    @GetMapping
    @Operation(
            summary = "Search resources",
            description = "Returns resources with optional filtering by type, active status, and name."
    )
    public List<ResourceResponse> findAll(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String name
    ) {
        ResourceFilter filter = new ResourceFilter(type, active, name);

        return resourceService.findAll(filter);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a resource",
            description = "Returns a resource by its ID."
    )
    public ResourceResponse findById(@PathVariable Long id) {
        return resourceService.findById(id);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(
            summary = "Deactivate a resource",
            description = "Deactivates a resource so that new reservations cannot be created for it."
    )
    public ResourceResponse deactivate(@PathVariable Long id) {
        return resourceService.deactivate(id);
    }
}