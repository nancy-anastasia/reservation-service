package com.nancyk.reservation.reservation;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return reservationService.create(request);
    }

    @GetMapping
    public Page<ReservationResponse> findAll(
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String reservedBy,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            Pageable pageable
    ) {
        ReservationFilter filter = new ReservationFilter(
                resourceId,
                status,
                reservedBy,
                from,
                to
        );

        return reservationService.findAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public ReservationResponse findById(@PathVariable Long id) {
        return reservationService.findById(id);
    }

    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }
}