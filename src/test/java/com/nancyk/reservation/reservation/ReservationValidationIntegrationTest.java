package com.nancyk.reservation.reservation;

import com.nancyk.reservation.AbstractIntegrationTest;
import com.nancyk.reservation.resource.Resource;
import com.nancyk.reservation.resource.ResourceTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationValidationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceTestFactory resourceTestFactory;

    @Test
    void shouldReturnNotFoundWhenResourceDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": 999999,
                                  "reservedBy": "Nancy",
                                  "startsAt": "2026-08-26T09:00:00Z",
                                  "endsAt": "2026-08-26T10:00:00Z"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource not found: 999999"))
                .andExpect(jsonPath("$.path").value("/api/reservations"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldRejectReservationWhenEndIsBeforeStart() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "resourceId": %d,
                              "reservedBy": "Nancy",
                              "startsAt": "2026-08-26T10:00:00Z",
                              "endsAt": "2026-08-26T09:00:00Z"
                            }
                            """.formatted(resource.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Reservation end time must be after start time"));
    }

    @Test
    void shouldRejectReservationWhenStartEqualsEnd() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "resourceId": %d,
                              "reservedBy": "Nancy",
                              "startsAt": "2026-08-26T09:00:00Z",
                              "endsAt": "2026-08-26T09:00:00Z"
                            }
                            """.formatted(resource.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Reservation end time must be after start time"));
    }

    @Test
    void shouldRejectReservationWithoutReservedBy() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "resourceId": %d,
                              "startsAt": "2026-08-26T09:00:00Z",
                              "endsAt": "2026-08-26T10:00:00Z"
                            }
                            """.formatted(resource.getId())))
                .andExpect(status().isBadRequest());
    }
}