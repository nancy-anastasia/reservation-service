package com.nancyk.reservation.reservation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/reservations")
@Tag(
        name = "Reservations",
        description = "Create, search, retrieve, and cancel reservations"
)
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a reservation",
            description = "Creates a confirmed reservation for an active resource."
    )
    public ReservationResponse create(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return reservationService.create(request);
    }

    @GetMapping
    @Operation(
            summary = "Search reservations",
            description = "Returns reservations with optional filtering, pagination, and sorting."
    )
    public Page<ReservationResponse> findAll(
            @Parameter(description = "Filter by resource ID")
            @RequestParam(required = false) Long resourceId,

            @Parameter(description = "Filter by reservation status")
            @RequestParam(required = false) ReservationStatus status,

            @Parameter(
                    description = "Partial match on the person who made the reservation"
            )
            @RequestParam(required = false) String reservedBy,

            @Parameter(
                    description = "Start of the search range. Reservations must end after this instant."
            )
            @RequestParam(required = false) Instant from,

            @Parameter(
                    description = "End of the search range. Reservations must start before this instant."
            )
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
    @Operation(
            summary = "Get a reservation",
            description = "Returns a reservation by its ID."
    )
    public ReservationResponse findById(@PathVariable Long id) {
        return reservationService.findById(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel a reservation",
            description = "Cancels an existing confirmed reservation."
    )
    public ReservationResponse cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }
}