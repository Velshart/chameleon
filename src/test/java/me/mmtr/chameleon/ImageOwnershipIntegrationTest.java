package me.mmtr.chameleon;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImageOwnershipIntegrationTest extends ChameleonIntegrationTest {
    @Test
    void shouldNotAllowAccessToAnotherUsersImage() throws Exception {
        String authorizationHeaderA = "Bearer " + registerAndGetToken(
                "username_a",
                "password_a"
        );

        String authorizationHeaderB = "Bearer " + registerAndGetToken(
                "username_b",
                "password_b"
        );

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "cat.png",
                MediaType.IMAGE_PNG_VALUE,
                createTestPng()
        );

        MvcResult uploadResult =
                mockMvc.perform(multipart("/api/images")
                                .file(mockMultipartFile)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationHeaderA
                                ))
                        .andExpect(status().isCreated())
                        .andReturn();

        Long imageId = objectMapper
                .readTree(uploadResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/images/{id}", imageId)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                authorizationHeaderB
                        ))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/images/{id}", imageId)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                authorizationHeaderB
                        ))
                .andExpect(status().isNotFound());

        String transformationsList = """
                { "transformations": [ { "type": "GRAYSCALE" } ] }
                """;
        mockMvc.perform(post("/api/images/{id}/transform", imageId)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                authorizationHeaderB
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                transformationsList
                        ))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/images/{id}", imageId)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                authorizationHeaderA
                        ))
                .andExpect(status().isOk());
    }
}
