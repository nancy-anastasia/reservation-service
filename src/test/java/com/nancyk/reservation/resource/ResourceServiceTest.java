package com.nancyk.reservation.resource;

import com.nancyk.reservation.common.exception.ResourceAlreadyInactiveException;
import com.nancyk.reservation.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(resourceRepository);
    }

    @Test
    void shouldCreateResource() {
        CreateResourceRequest request = new CreateResourceRequest(
                "Conference Room A",
                "Large conference room",
                ResourceType.MEETING_ROOM
        );

        when(resourceRepository.save(any(Resource.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResourceResponse response = resourceService.create(request);

        assertThat(response.name()).isEqualTo("Conference Room A");
        assertThat(response.description()).isEqualTo("Large conference room");
        assertThat(response.type()).isEqualTo(ResourceType.MEETING_ROOM);
        assertThat(response.active()).isTrue();

        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void shouldFindResourceById() {
        Resource resource = new Resource(
                "Conference Room A",
                "Large conference room",
                ResourceType.MEETING_ROOM
        );

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        ResourceResponse response = resourceService.findById(1L);

        assertThat(response.name()).isEqualTo("Conference Room A");
        assertThat(response.type()).isEqualTo(ResourceType.MEETING_ROOM);
        assertThat(response.active()).isTrue();

        verify(resourceRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenResourceDoesNotExist() {
        when(resourceRepository.findById(42L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resource not found: 42");
    }

    @Test
    void shouldDeactivateResource() {
        Resource resource = new Resource(
                "Conference Room A",
                "Large conference room",
                ResourceType.MEETING_ROOM
        );

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        when(resourceRepository.saveAndFlush(resource))
                .thenReturn(resource);

        ResourceResponse response = resourceService.deactivate(1L);

        assertThat(response.active()).isFalse();
        assertThat(resource.isInactive()).isTrue();

        verify(resourceRepository).findById(1L);
        verify(resourceRepository).saveAndFlush(resource);
    }

    @Test
    void shouldThrowWhenDeactivatingMissingResource() {
        when(resourceRepository.findById(42L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.deactivate(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resource not found: 42");

        verify(resourceRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowWhenResourceIsAlreadyInactive() {
        Resource resource = new Resource(
                "Conference Room A",
                "Large conference room",
                ResourceType.MEETING_ROOM
        );

        resource.deactivate();

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        assertThatThrownBy(() -> resourceService.deactivate(1L))
                .isInstanceOf(ResourceAlreadyInactiveException.class)
                .hasMessage("Resource is already inactive: 1");

        verify(resourceRepository, never()).saveAndFlush(any());
    }
}