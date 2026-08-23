package com.nancyk.reservation.resource;

import com.nancyk.reservation.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void shouldPersistAndLoadResource() {
        Resource resource = new Resource(
                "Projector A",
                "4K conference projector",
                ResourceType.EQUIPMENT
        );

        Resource saved = resourceRepository.save(resource);

        Optional<Resource> found = resourceRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Projector A");
        assertThat(found.get().getType()).isEqualTo(ResourceType.EQUIPMENT);
        assertThat(found.get().isActive()).isTrue();
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldGenerateIdWhenResourceIsPersisted() {
        Resource resource = new Resource(
                "Desk 42",
                "Standing desk",
                ResourceType.DESK
        );

        Resource saved = resourceRepository.save(resource);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldReturnEmptyWhenResourceDoesNotExist() {
        Optional<Resource> result =
                resourceRepository.findById(Long.MAX_VALUE);

        assertThat(result).isEmpty();
    }
}