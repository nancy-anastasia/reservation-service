package com.nancyk.reservation.reservation;

import com.nancyk.reservation.AbstractIntegrationTest;
import com.nancyk.reservation.resource.Resource;
import com.nancyk.reservation.resource.ResourceTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationLifecycleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceTestFactory resourceTestFactory;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetReservationById() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        Long reservationId = createReservation(resource);

        mockMvc.perform(get("/api/reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.resourceId").value(resource.getId()))
                .andExpect(jsonPath("$.reservedBy").value("Nancy"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldListReservations() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        createReservation(resource);

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reservedBy").value("Nancy"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void shouldReturnNotFoundWhenReservationDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/reservations/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Reservation not found: 999999"));
    }

    @Test
    void shouldCancelReservation() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        Long reservationId = createReservation(resource);

        mockMvc.perform(post("/api/reservations/{id}/cancel", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldReturnNotFoundWhenCancellingMissingReservation() throws Exception {
        mockMvc.perform(post("/api/reservations/{id}/cancel", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Reservation not found: 999999"));
    }

    @Test
    void shouldRejectCancellingReservationTwice() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        Long reservationId = createReservation(resource);

        mockMvc.perform(post("/api/reservations/{id}/cancel", reservationId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reservations/{id}/cancel", reservationId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Reservation is already cancelled: " + reservationId));
    }

    @Test
    void shouldAllowNewReservationAfterPreviousOneIsCancelled() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        Long reservationId = createReservation(resource);

        mockMvc.perform(post("/api/reservations/{id}/cancel", reservationId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d,
                                  "reservedBy": "Alice",
                                  "startsAt": "2026-08-26T09:00:00Z",
                                  "endsAt": "2026-08-26T10:00:00Z"
                                }
                                """.formatted(resource.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        long confirmedReservations = reservationRepository.findAll()
                .stream()
                .filter(reservation ->
                        reservation.getStatus() == ReservationStatus.CONFIRMED)
                .count();

        assertThat(confirmedReservations).isEqualTo(1);
    }

    private Long createReservation(Resource resource) throws Exception {
        String response = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d,
                                  "reservedBy": "Nancy",
                                  "startsAt": "2026-08-26T09:00:00Z",
                                  "endsAt": "2026-08-26T10:00:00Z"
                                }
                                """.formatted(resource.getId())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response)
                .get("id")
                .asLong();
    }
}