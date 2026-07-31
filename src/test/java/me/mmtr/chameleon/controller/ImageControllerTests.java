package me.mmtr.chameleon.controller;

import me.mmtr.chameleon.exception.ImageNotFoundException;
import me.mmtr.chameleon.filter.JwtAuthenticationFilter;
import me.mmtr.chameleon.filter.RateLimitingFilter;
import me.mmtr.chameleon.handler.GlobalExceptionHandler;
import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.assembler.ImageModelAssembler;
import me.mmtr.chameleon.model.dto.request.ImageResponse;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ImageController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, RateLimitingFilter.class}
        )
)
@Import({ImageModelAssembler.class, GlobalExceptionHandler.class})
public class ImageControllerTests {

    private static final User CURRENT_USER = new User(
            "test_user",
            "password"
    );

    @MockitoBean
    private ImageService imageService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn201WithImageDetailsAndLinksAfterUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cat.jpg", MediaType.IMAGE_JPEG_VALUE, "image-bytes".getBytes()
        );

        ImageResponse response = new ImageResponse(
                1L, "cat.jpg", MediaType.IMAGE_JPEG_VALUE, 17L, Instant.parse("2026-07-29T10:00:00Z")
        );

        when(imageService.upload(any(), eq(CURRENT_USER))).thenReturn(response);

        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .with(user(CURRENT_USER))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(1L))
                .andExpect(jsonPath("$.originalFilename")
                        .value("cat.jpg"))
                .andExpect(jsonPath("$._links.self.href")
                        .value("http://localhost/api/images/1"))
                .andExpect(jsonPath("$._links.delete.href")
                        .value("http://localhost/api/images/1"))
                .andExpect(jsonPath("$._links.images.href")
                        .value("http://localhost/api/images"));
    }

    @Test
    void shouldReturnListOfOwnedImagesWithLinks() throws Exception {
        ImageResponse response = new ImageResponse(
                1L, "cat.jpg", MediaType.IMAGE_JPEG_VALUE, 17L, Instant.parse("2026-07-29T10:00:00Z")
        );

        when(imageService.getAllForOwner(CURRENT_USER)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/images")
                        .with(user(CURRENT_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.imageResponseList[0].id")
                        .value(1))
                .andExpect(jsonPath("$._embedded.imageResponseList[0]._links.self.href")
                        .value("http://localhost/api/images/1"))
                .andExpect(jsonPath("$._links.self.href")
                        .value("http://localhost/api/images"));
    }

    @Test
    void shouldReturnEmptyListWithLinkWhenUserHasNoImages() throws Exception {
        when(imageService.getAllForOwner(CURRENT_USER))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/images")
                        .with(user(CURRENT_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.imageResponseList")
                        .doesNotExist())
                .andExpect(jsonPath("$._links.self.href")
                        .value("http://localhost/api/images")
                );
    }

    @Test
    void shouldReturnImageBytesWithCorrectContentType() throws Exception {
        Long imageId = 1L;

        Image image = new Image(
                "cat.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                17L,
                CURRENT_USER
        );
        byte[] imageBytes = "image-bytes".getBytes();

        when(imageService.getImageForOwner(imageId, CURRENT_USER))
                .thenReturn(image);
        when(imageService.getImageData(imageId, CURRENT_USER))
                .thenReturn(imageBytes);

        mockMvc.perform(get("/api/images/" + imageId)
                        .with(user(CURRENT_USER)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string(
                        "Content-Disposition",
                        "inline; filename=\"cat.jpg\""
                ))
                .andExpect(content().bytes(imageBytes));
    }

    @Test
    void shouldReturn404WhenImageNotFoundForUser() throws Exception {
        Long imageId = 1000L;
        when(imageService.getImageForOwner(imageId, CURRENT_USER))
                .thenThrow(new ImageNotFoundException(imageId));

        mockMvc.perform(get("/api/images/" + imageId)
                        .with(user(CURRENT_USER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNoContentAfterSuccessfulDelete() throws Exception {
        long imageId = 1L;

        mockMvc.perform(delete("/api/images/" + imageId)
                        .with(user(CURRENT_USER))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonOwnedImage() throws Exception {
        long imageId = 1L;

        org.mockito.Mockito.doThrow(new ImageNotFoundException(imageId))
                .when(imageService).delete(imageId, CURRENT_USER);

        mockMvc.perform(delete("/api/images/" + imageId)
                        .with(user(CURRENT_USER))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}