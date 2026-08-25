package com.nancyk.reservation.reservation;

import com.nancyk.reservation.AbstractIntegrationTest;
import com.nancyk.reservation.resource.Resource;
import com.nancyk.reservation.resource.ResourceTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationPaginationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceTestFactory resourceTestFactory;

    @Test
    void shouldReturnFirstPageOfReservations() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        createReservations(resource);

        mockMvc.perform(get("/api/reservations")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "startsAt,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].reservedBy").value("Nancy"))
                .andExpect(jsonPath("$.content[1].reservedBy").value("Alice"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.numberOfElements").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void shouldReturnSecondPageOfReservations() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        createReservations(resource);

        mockMvc.perform(get("/api/reservations")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "startsAt,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].reservedBy").value("Bob"))
                .andExpect(jsonPath("$.content[1].reservedBy").value("Carol"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.numberOfElements").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldSortReservationsByStartTimeDescending() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        createReservations(resource);

        mockMvc.perform(get("/api/reservations")
                        .param("sort", "startsAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].reservedBy").value("Carol"))
                .andExpect(jsonPath("$.content[1].reservedBy").value("Bob"))
                .andExpect(jsonPath("$.content[2].reservedBy").value("Alice"))
                .andExpect(jsonPath("$.content[3].reservedBy").value("Nancy"));
    }

    private void createReservations(Resource resource) throws Exception {
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

        createReservation(
                resource,
                "Bob",
                "2026-08-26T11:00:00Z",
                "2026-08-26T12:00:00Z"
        );

        createReservation(
                resource,
                "Carol",
                "2026-08-26T12:00:00Z",
                "2026-08-26T13:00:00Z"
        );
    }

    private void createReservation(
            Resource resource,
            String reservedBy,
            String startsAt,
            String endsAt
    ) throws Exception {
        mockMvc.perform(post("/api/reservations")
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
                .andExpect(status().isCreated());
    }
}
