package com.nancyk.reservation.reservation;

import com.nancyk.reservation.common.exception.InvalidReservationPeriodException;
import com.nancyk.reservation.common.exception.ReservationConflictException;
import com.nancyk.reservation.common.exception.ResourceNotFoundException;
import com.nancyk.reservation.resource.Resource;
import com.nancyk.reservation.resource.ResourceRepository;
import com.nancyk.reservation.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                resourceRepository
        );
    }

    @Test
    void shouldCreateReservationForActiveResource() {
        Resource resource = new Resource(
                "Conference Room A",
                "Large conference room",
                ResourceType.MEETING_ROOM
        );

        CreateReservationRequest request = new CreateReservationRequest(
                1L,
                "Nancy",
                Instant.parse("2026-08-26T09:00:00Z"),
                Instant.parse("2026-08-26T10:00:00Z")
        );

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository.saveAndFlush(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response = reservationService.create(request);

        assertThat(response.reservedBy()).isEqualTo("Nancy");
        assertThat(response.startsAt())
                .isEqualTo(Instant.parse("2026-08-26T09:00:00Z"));
        assertThat(response.endsAt())
                .isEqualTo(Instant.parse("2026-08-26T10:00:00Z"));
        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);

        verify(resourceRepository).findById(1L);
        verify(reservationRepository).saveAndFlush(any(Reservation.class));
    }

    @Test
    void shouldThrowWhenResourceDoesNotExist() {
        when(resourceRepository.findById(42L))
                .thenReturn(Optional.empty());

        CreateReservationRequest request = new CreateReservationRequest(
                42L,
                "Nancy",
                Instant.parse("2026-08-26T09:00:00Z"),
                Instant.parse("2026-08-26T10:00:00Z")
        );

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resource not found: 42");

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowWhenReservationPeriodIsInvalid() {
        Resource resource = new Resource(
                "Conference Room A",
                "Large conference room",
                ResourceType.MEETING_ROOM
        );

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        CreateReservationRequest request = new CreateReservationRequest(
                1L,
                "Nancy",
                Instant.parse("2026-08-26T10:00:00Z"),
                Instant.parse("2026-08-26T09:00:00Z")
        );

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(InvalidReservationPeriodException.class);

        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowWhenReservationOverlapsExistingReservation() {
        Resource resource = new Resource(
                "Conference Room A",
                "Large conference room",
                ResourceType.MEETING_ROOM
        );

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository.existsOverlappingReservation(
                        1L,
                        ReservationStatus.CONFIRMED,
                        Instant.parse("2026-08-26T09:30:00Z"),
                        Instant.parse("2026-08-26T10:30:00Z")
                ))
                .thenReturn(true);

        CreateReservationRequest request = new CreateReservationRequest(
                1L,
                "Nancy",
                Instant.parse("2026-08-26T09:30:00Z"),
                Instant.parse("2026-08-26T10:30:00Z")
        );

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("Resource already has an overlapping reservation: 1");

        verify(reservationRepository, never()).saveAndFlush(any());
    }
}
