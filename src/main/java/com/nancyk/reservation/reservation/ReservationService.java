package com.nancyk.reservation.reservation;

import com.nancyk.reservation.common.exception.InvalidReservationPeriodException;
import com.nancyk.reservation.common.exception.ReservationAlreadyCancelledException;
import com.nancyk.reservation.common.exception.ReservationConflictException;
import com.nancyk.reservation.common.exception.ReservationNotFoundException;
import com.nancyk.reservation.common.exception.ResourceInactiveException;
import com.nancyk.reservation.common.exception.ResourceNotFoundException;
import com.nancyk.reservation.resource.Resource;
import com.nancyk.reservation.resource.ResourceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        Resource resource = resourceRepository.findByIdForUpdate(request.resourceId())
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

    public ReservationResponse findById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new ReservationNotFoundException(reservationId)
                );

        return ReservationResponse.from(reservation);
    }

    public Page<ReservationResponse> findAll(
            ReservationFilter filter,
            Pageable pageable
    ) {
        var specification =
                ReservationSpecifications.hasResourceId(filter.resourceId())
                        .and(ReservationSpecifications.hasStatus(filter.status()))
                        .and(ReservationSpecifications.reservedByContains(
                                filter.reservedBy()
                        ))
                        .and(ReservationSpecifications.overlapsTimeRange(
                                filter.from(),
                                filter.to()
                        ));

        return reservationRepository.findAll(specification, pageable)
                .map(ReservationResponse::from);
    }

    @Transactional
    public ReservationResponse cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new ReservationNotFoundException(reservationId)
                );

        if (reservation.isCancelled()) {
            throw new ReservationAlreadyCancelledException(reservationId);
        }

        reservation.cancel();

        Reservation saved = reservationRepository.saveAndFlush(reservation);

        return ReservationResponse.from(saved);
    }
}