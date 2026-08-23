package com.nancyk.reservation.resource;

import com.nancyk.reservation.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResourceValidationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectResourceWithoutName() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Large conference room",
                                  "type": "MEETING_ROOM"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBlankResourceName() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "   ",
                              "description": "Large conference room",
                              "type": "MEETING_ROOM"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectResourceNameLongerThan150Characters() throws Exception {
        String name = "A".repeat(151);

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "%s",
                              "description": "Large conference room",
                              "type": "MEETING_ROOM"
                            }
                            """.formatted(name)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectResourceWithoutType() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Conference Room A",
                                  "description": "Large conference room"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUnknownResourceType() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Conference Room A",
                              "description": "Large conference room",
                              "type": "SOMETHING_INVALID"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectDescriptionLongerThan500Characters() throws Exception {
        String description = "A".repeat(501);

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Conference Room A",
                              "description": "%s",
                              "type": "MEETING_ROOM"
                            }
                            """.formatted(description)))
                .andExpect(status().isBadRequest());
    }
}