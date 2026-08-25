package com.nancyk.reservation.reservation;

import com.nancyk.reservation.AbstractIntegrationTest;
import com.nancyk.reservation.common.exception.ReservationConflictException;
import com.nancyk.reservation.resource.Resource;
import com.nancyk.reservation.resource.ResourceTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ResourceTestFactory resourceTestFactory;

    @Test
    void shouldAllowOnlyOneConcurrentOverlappingReservation() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        CreateReservationRequest requestA = new CreateReservationRequest(
                resource.getId(),
                "Nancy",
                Instant.parse("2026-08-26T09:00:00Z"),
                Instant.parse("2026-08-26T10:00:00Z")
        );

        CreateReservationRequest requestB = new CreateReservationRequest(
                resource.getId(),
                "Alice",
                Instant.parse("2026-08-26T09:30:00Z"),
                Instant.parse("2026-08-26T10:30:00Z")
        );

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

            Future<Object> resultA = executor.submit(() ->
                    executeReservation(requestA, ready, start));

            Future<Object> resultB = executor.submit(() ->
                    executeReservation(requestB, ready, start));

            ready.await();

            // Release both threads at essentially the same time.
            start.countDown();

            Object outcomeA = resultA.get();
            Object outcomeB = resultB.get();

            List<Object> outcomes = List.of(outcomeA, outcomeB);

            long successes = outcomes.stream()
                    .filter(ReservationResponse.class::isInstance)
                    .count();

            long conflicts = outcomes.stream()
                    .filter(ReservationConflictException.class::isInstance)
                    .count();

            assertThat(successes).isEqualTo(1);
            assertThat(conflicts).isEqualTo(1);

            assertThat(reservationRepository.count()).isEqualTo(1);
        }
    }

    private Object executeReservation(
            CreateReservationRequest request,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        try {
            ready.countDown();
            start.await();

            return reservationService.create(request);
        } catch (ReservationConflictException exception) {
            return exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(exception);
        }
    }
}