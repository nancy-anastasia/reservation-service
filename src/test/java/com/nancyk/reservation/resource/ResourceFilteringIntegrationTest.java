package com.nancyk.reservation.resource;

import com.nancyk.reservation.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResourceFilteringIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceTestFactory resourceTestFactory;

    @Test
    void shouldFilterResourcesByNameIgnoringCase() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Conference Room A",
                                  "description": "Large conference room",
                                  "type": "MEETING_ROOM"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Standing Desk",
                                  "description": "Adjustable desk",
                                  "type": "DESK"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/resources")
                        .param("name", "conference"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Conference Room A"));
    }

    @Test
    void shouldFilterResourcesByType() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Conference Room A",
                              "description": "Large conference room",
                              "type": "MEETING_ROOM"
                            }
                            """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Standing Desk",
                              "description": "Adjustable desk",
                              "type": "DESK"
                            }
                            """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/resources")
                        .param("type", "MEETING_ROOM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Conference Room A"))
                .andExpect(jsonPath("$[0].type").value("MEETING_ROOM"));
    }

    @Test
    void shouldFilterResourcesByActiveStatus() throws Exception {
        Resource activeResource = resourceTestFactory.createActiveResource();
        Resource inactiveResource = resourceTestFactory.createActiveResource();

        mockMvc.perform(post(
                        "/api/resources/{id}/deactivate",
                        inactiveResource.getId()
                ))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/resources")
                        .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(inactiveResource.getId()))
                .andExpect(jsonPath("$[0].active").value(false));
    }

    @Test
    void shouldCombineTypeAndActiveFilters() throws Exception {
        Resource activeResource = resourceTestFactory.createActiveResource();
        Resource inactiveResource = resourceTestFactory.createActiveResource();

        mockMvc.perform(post(
                        "/api/resources/{id}/deactivate",
                        inactiveResource.getId()
                ))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/resources")
                        .param("type", "MEETING_ROOM")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(activeResource.getId()));
    }
}
