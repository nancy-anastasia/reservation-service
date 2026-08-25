package com.nancyk.reservation.resource;

import com.nancyk.reservation.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResourceLifecycleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceTestFactory resourceTestFactory;

    @Test
    void shouldDeactivateResource() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        mockMvc.perform(post("/api/resources/{id}/deactivate", resource.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resource.getId()))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldReturnNotFoundWhenDeactivatingMissingResource() throws Exception {
        mockMvc.perform(post("/api/resources/{id}/deactivate", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Resource not found: 999999"));
    }

    @Test
    void shouldRejectDeactivatingResourceTwice() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        mockMvc.perform(post("/api/resources/{id}/deactivate", resource.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/resources/{id}/deactivate", resource.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Resource is already inactive: " + resource.getId()));
    }

    @Test
    void shouldRejectReservationForInactiveResource() throws Exception {
        Resource resource = resourceTestFactory.createActiveResource();

        mockMvc.perform(post("/api/resources/{id}/deactivate", resource.getId()))
                .andExpect(status().isOk());

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
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Resource is inactive: " + resource.getId()));
    }
}