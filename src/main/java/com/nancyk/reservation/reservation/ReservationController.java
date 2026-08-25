package com.nancyk.reservation.reservation;

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
    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
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