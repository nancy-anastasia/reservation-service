package com.nancyk.reservation.reservation;

import com.nancyk.reservation.common.exception.InvalidReservationPeriodException;
import com.nancyk.reservation.common.exception.ReservationConflictException;
import com.nancyk.reservation.common.exception.ResourceInactiveException;
import com.nancyk.reservation.common.exception.ResourceNotFoundException;
import com.nancyk.reservation.resource.Resource;
import com.nancyk.reservation.resource.ResourceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public ReservationResponse create(CreateReservationRequest request) {
        Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(request.resourceId())
                );

        if (!resource.isActive()) {
            throw new ResourceInactiveException(resource.getId());
        }

        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new InvalidReservationPeriodException();
        }

        boolean overlaps =
                reservationRepository.existsOverlappingReservation(
                        request.resourceId(),
                        ReservationStatus.CONFIRMED,
                        request.startsAt(),
                        request.endsAt()
                );

        if (overlaps) {
            throw new ReservationConflictException(request.resourceId());
        }

        Reservation reservation = new Reservation(
                resource,
                request.reservedBy(),
                request.startsAt(),
                request.endsAt()
        );

        try {
            Reservation saved = reservationRepository.saveAndFlush(reservation);
            return ReservationResponse.from(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new ReservationConflictException(request.resourceId());
        }
    }
}