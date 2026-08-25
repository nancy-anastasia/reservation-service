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

class ReservationControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceTestFactory resourceTestFactory;

    @Test
    void shouldCreateReservation() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        mockMvc.perform(post("/api/reservations")
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
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.resourceId").value(resource.getId()))
                .andExpect(jsonPath("$.reservedBy").value("Nancy"))
                .andExpect(jsonPath("$.startsAt")
                        .value("2026-08-26T09:00:00Z"))
                .andExpect(jsonPath("$.endsAt")
                        .value("2026-08-26T10:00:00Z"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.createdAt").exists());
    }
}