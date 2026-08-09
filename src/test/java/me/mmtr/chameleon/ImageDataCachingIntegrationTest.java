package me.mmtr.chameleon;

import me.mmtr.chameleon.repository.ImageDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImageDataCachingIntegrationTest extends ChameleonIntegrationTest {

    private String authorizationHeader;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private ImageDataRepository imageDataRepository;

    @BeforeEach
    void beforeEach() throws Exception {
        String jwtToken = registerAndGetToken(
                "username",
                "password"
        );

        authorizationHeader = "Bearer " + jwtToken;

        Objects.requireNonNull(cacheManager.getCache("image_data"))
                .clear();
    }

    @Test
    void shouldSupportImageLifecycleWithCaching() throws Exception {
        byte[] testImageBytes = createTestPng();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "cat.png",
                MediaType.IMAGE_PNG_VALUE,
                testImageBytes
        );

        MvcResult uploadResult = mockMvc.perform(
                        multipart("/api/images")
                                .file(mockMultipartFile)
                                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                ).andExpect(status().isCreated())
                .andReturn();

        Long imageId = objectMapper
                .readTree(uploadResult.getResponse().getContentAsString())
                .get("id").asLong();

        clearInvocations(imageDataRepository);

        mockMvc.perform(
                        get("/api/images/{id}", imageId)
                                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                ).andExpect(status().isOk())
                .andExpect(content().bytes(testImageBytes))
                .andReturn();

        verify(imageDataRepository, times(1))
                .findById(imageId);

        byte[] cachedImageBytes =
                Objects.requireNonNull(cacheManager.getCache("image_data"))
                        .get(imageId, byte[].class);

        assertNotNull(cachedImageBytes);
        assertArrayEquals(testImageBytes, cachedImageBytes);

        mockMvc.perform(get("/api/images/{id}", imageId)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                authorizationHeader
                        ))
                .andExpect(status().isOk())
                .andExpect(content().bytes(testImageBytes));

        verify(imageDataRepository, times(1))
                .findById(imageId);

        String transformationsList = """
                { "transformations": [ { "type": "RESIZE", "width": 20, "height": 20 } ] }
                """;

        MvcResult transformationResult =
                mockMvc.perform(post("/api/images/{id}/transform", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transformationsList))
                .andExpect(status().isOk())
                .andReturn();

        verify(imageDataRepository, times(2))
                .findById(imageId);

        testImageBytes = transformationResult.getResponse().getContentAsByteArray();

        assertNull(Objects.requireNonNull(cacheManager.getCache("image_data"))
                .get(imageId, byte[].class)
        );

        mockMvc.perform(
                        get("/api/images/{id}", imageId)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationHeader
                                ))
                .andExpect(status().isOk())
                .andExpect(content().bytes(testImageBytes))
                .andReturn();

        verify(imageDataRepository, times(3))
                .findById(imageId);

        mockMvc.perform(delete("/api/images/{id}", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .andExpect(status().isNoContent());

        assertNull(Objects.requireNonNull(cacheManager.getCache("image_data"))
                .get(imageId, byte[].class)
        );

        mockMvc.perform(get("/api/images/{id}", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .andExpect(status().isNotFound());
    }
}
