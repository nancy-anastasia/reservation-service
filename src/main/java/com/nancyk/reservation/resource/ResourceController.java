package com.nancyk.reservation.resource;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse create(
            @Valid @RequestBody CreateResourceRequest request
    ) {
        return resourceService.create(request);
    }

    @GetMapping
    public List<ResourceResponse> findAll() {
        return resourceService.findAll();
    }

    @PostMapping("/{id}/deactivate")
    public ResourceResponse deactivate(@PathVariable Long id) {
        return resourceService.deactivate(id);
    }
}