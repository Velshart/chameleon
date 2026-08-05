package me.mmtr.chameleon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ImageLifecycleIntegrationTest extends ChameleonIntegrationTest {
    private String authorizationHeader;

    @BeforeEach
    void beforeEach() throws Exception {
        String jwtToken = registerAndGetToken(
                "username",
                "password"
        );

        authorizationHeader = "Bearer " + jwtToken;
    }

    @Test
    void shouldSupportFullImageLifecycle() throws Exception {
        byte[] testImageBytes = createTestPng();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "cat.png",
                MediaType.IMAGE_PNG_VALUE,
                testImageBytes
        );

        MvcResult imageUploadResult = mockMvc.perform(
                        multipart("/api/images")
                                .file(mockMultipartFile)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationHeader
                                ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename")
                        .value("cat.png"))
                .andReturn();

        Long imageId = objectMapper
                .readTree(imageUploadResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/images")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                authorizationHeader
                        ))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$._embedded.images[0].id")
                                .value(imageId)
                );

        mockMvc.perform(get("/api/images/{id}", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(content().bytes(testImageBytes));

        mockMvc.perform(delete("/api/images/{id}", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/images/{id}", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .andExpect(status().isNotFound());
    }
}
