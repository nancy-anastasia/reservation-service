package com.nancyk.reservation.reservation;

import com.nancyk.reservation.resource.Resource;
import com.nancyk.reservation.resource.ResourceRepository;
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
                        new IllegalArgumentException(
                                "Resource not found: " + request.resourceId()
                        )
                );

        if (!resource.isActive()) {
            throw new IllegalStateException("Resource is not active");
        }

        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new IllegalArgumentException(
                    "Reservation end time must be after start time"
            );
        }

        Reservation reservation = new Reservation(
                resource,
                request.reservedBy(),
                request.startsAt(),
                request.endsAt()
        );

        Reservation saved = reservationRepository.save(reservation);

        return ReservationResponse.from(saved);
    }
}