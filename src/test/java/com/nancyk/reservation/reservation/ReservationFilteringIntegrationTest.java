package com.nancyk.reservation.reservation;

import com.nancyk.reservation.AbstractIntegrationTest;
import com.nancyk.reservation.resource.Resource;
import com.nancyk.reservation.resource.ResourceTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationFilteringIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceTestFactory resourceTestFactory;

    @Test
    void shouldReturnAllReservationsWhenNoFiltersAreProvided() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        createReservation(
                resource,
                "Nancy",
                "2026-08-26T09:00:00Z",
                "2026-08-26T10:00:00Z"
        );

        createReservation(
                resource,
                "Alice",
                "2026-08-26T10:00:00Z",
                "2026-08-26T11:00:00Z"
        );

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldFilterReservationsByStatus() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        createReservation(
                resource,
                "Nancy",
                "2026-08-26T09:00:00Z",
                "2026-08-26T10:00:00Z"
        );

        Long cancelledReservationId = createReservation(
                resource,
                "Alice",
                "2026-08-26T10:00:00Z",
                "2026-08-26T11:00:00Z"
        );

        mockMvc.perform(post(
                        "/api/reservations/{id}/cancel",
                        cancelledReservationId
                ))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].reservedBy").value("Nancy"))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldFilterReservationsByResourceId() throws Exception {
        Resource resourceA = resourceTestFactory.createActiveResource();
        Resource resourceB = resourceTestFactory.createActiveResource();

        createReservation(
                resourceA,
                "Nancy",
                "2026-08-26T09:00:00Z",
                "2026-08-26T10:00:00Z"
        );

        createReservation(
                resourceB,
                "Alice",
                "2026-08-26T09:00:00Z",
                "2026-08-26T10:00:00Z"
        );

        mockMvc.perform(get("/api/reservations")
                        .param("resourceId", resourceA.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].resourceId").value(resourceA.getId()))
                .andExpect(jsonPath("$.content[0].reservedBy").value("Nancy"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldFilterReservationsByReservedByIgnoringCase() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        createReservation(
                resource,
                "Nancy",
                "2026-08-26T09:00:00Z",
                "2026-08-26T10:00:00Z"
        );

        createReservation(
                resource,
                "Alice",
                "2026-08-26T10:00:00Z",
                "2026-08-26T11:00:00Z"
        );

        mockMvc.perform(get("/api/reservations")
                        .param("reservedBy", "nancy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].reservedBy").value("Nancy"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldCombineResourceAndStatusFilters() throws Exception {
        Resource resourceA = resourceTestFactory.createActiveResource();
        Resource resourceB = resourceTestFactory.createActiveResource();

        createReservation(
                resourceA,
                "Nancy",
                "2026-08-26T09:00:00Z",
                "2026-08-26T10:00:00Z"
        );

        Long cancelledReservationId = createReservation(
                resourceA,
                "Alice",
                "2026-08-26T10:00:00Z",
                "2026-08-26T11:00:00Z"
        );

        mockMvc.perform(post(
                        "/api/reservations/{id}/cancel",
                        cancelledReservationId
                ))
                .andExpect(status().isOk());

        createReservation(
                resourceB,
                "Bob",
                "2026-08-26T09:00:00Z",
                "2026-08-26T10:00:00Z"
        );

        mockMvc.perform(get("/api/reservations")
                        .param("resourceId", resourceA.getId().toString())
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].reservedBy").value("Nancy"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnReservationOverlappingRequestedTimeRange() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        createReservation(
                resource,
                "Nancy",
                "2026-08-26T09:00:00Z",
                "2026-08-26T10:00:00Z"
        );

        createReservation(
                resource,
                "Alice",
                "2026-08-26T11:00:00Z",
                "2026-08-26T12:00:00Z"
        );

        mockMvc.perform(get("/api/reservations")
                        .param("from", "2026-08-26T09:30:00Z")
                        .param("to", "2026-08-26T10:30:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].reservedBy").value("Nancy"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldNotReturnReservationEndingWhenRequestedRangeStarts() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        createReservation(
                resource,
                "Nancy",
                "2026-08-26T09:00:00Z",
                "2026-08-26T10:00:00Z"
        );

        mockMvc.perform(get("/api/reservations")
                        .param("from", "2026-08-26T10:00:00Z")
                        .param("to", "2026-08-26T11:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    private Long createReservation(
            Resource resource,
            String reservedBy,
            String startsAt,
            String endsAt
    ) throws Exception {
        String response = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": %d,
                                  "reservedBy": "%s",
                                  "startsAt": "%s",
                                  "endsAt": "%s"
                                }
                                """.formatted(
                                resource.getId(),
                                reservedBy,
                                startsAt,
                                endsAt
                        )))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response)
                .get("id")
                .asLong();
    }
}