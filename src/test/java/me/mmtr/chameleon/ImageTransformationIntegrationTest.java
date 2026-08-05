package me.mmtr.chameleon;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImageTransformationIntegrationTest extends ChameleonIntegrationTest {
    @Test
    void shouldTransformAndPersistTransformedImageInDatabase() throws Exception {
        String authorizationHeader = "Bearer " + registerAndGetToken(
                "username",
                "password"
        );

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "cat.png",
                MediaType.IMAGE_PNG_VALUE,
                createTestPng());

        MvcResult uploadResult =
                mockMvc.perform(multipart("/api/images")
                                .file(mockMultipartFile)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationHeader
                                ))
                        .andExpect(status().isCreated())
                        .andReturn();

        Long imageId = objectMapper
                .readTree(uploadResult.getResponse().getContentAsString())
                .get("id").asLong();

        String transformationsList = """
                { "transformations": [ { "type": "RESIZE", "width": 20, "height": 20 } ] }
                """;

        mockMvc.perform(post("/api/images/{id}/transform", imageId)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transformationsList))
                .andExpect(status().isOk());

        MvcResult getImageResult =
                mockMvc.perform(get("/api/images/{id}", imageId)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationHeader
                                ))
                        .andExpect(status().isOk())
                        .andReturn();

        byte[] resultBytes = getImageResult.getResponse().getContentAsByteArray();
        BufferedImage resultImage =
                ImageIO.read(new ByteArrayInputStream(resultBytes));

        assertEquals(20, resultImage.getWidth());
        assertEquals(20, resultImage.getHeight());
    }
}
