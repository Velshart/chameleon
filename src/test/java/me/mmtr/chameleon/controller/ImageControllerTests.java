package me.mmtr.chameleon.controller;

import me.mmtr.chameleon.exception.ImageNotFoundException;
import me.mmtr.chameleon.filter.JwtAuthenticationFilter;
import me.mmtr.chameleon.filter.RateLimitingFilter;
import me.mmtr.chameleon.handler.GlobalExceptionHandler;
import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.assembler.ImageModelAssembler;
import me.mmtr.chameleon.model.dto.request.ImageResponse;
import me.mmtr.chameleon.model.dto.request.transformation.*;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.service.images.ImageService;
import me.mmtr.chameleon.service.images.ImageTransformationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
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

    private static final long IMAGE_ID = 1;

    @MockitoBean
    private ImageService imageService;

    @MockitoBean
    private ImageTransformationService transformationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn201WithImageDetailsAndLinksAfterUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cat.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image-bytes".getBytes()
        );

        ImageResponse response = new ImageResponse(
                IMAGE_ID,
                "cat.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                17L,
                Instant.parse("2026-07-29T10:00:00Z")
        );

        when(imageService.upload(any(), eq(CURRENT_USER))).thenReturn(response);

        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .with(user(CURRENT_USER))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(IMAGE_ID))
                .andExpect(jsonPath("$.originalFilename")
                        .value("cat.jpg"))
                .andExpect(jsonPath("$._links.self.href")
                        .value(endsWith("/api/images/1")))
                .andExpect(jsonPath("$._links.delete.href")
                        .value(endsWith("/api/images/1")))
                .andExpect(jsonPath("$._links.images.href")
                        .value(endsWith("/api/images")));
    }

    @Test
    void shouldReturnListOfOwnedImagesWithLinks() throws Exception {
        ImageResponse response = new ImageResponse(
                IMAGE_ID,
                "cat.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                17L,
                Instant.parse("2026-07-29T10:00:00Z")
        );

        Pageable pageable = PageRequest.of(0, 2);
        Page<ImageResponse> page =
                new PageImpl<>(List.of(response), pageable, 1);

        when(imageService.getAllForOwner(eq(CURRENT_USER), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/images")
                        .with(user(CURRENT_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.images[0].id")
                        .value(IMAGE_ID))
                .andExpect(jsonPath("$._embedded.images[0]._links.self.href")
                        .value(endsWith("/api/images/1")))
                .andExpect(jsonPath("$._links.self.href")
                        .value(endsWith("/api/images?page=0&size=2")))
                .andExpect(jsonPath("$.page.size")
                        .value(2))
                .andExpect(jsonPath("$.page.totalElements")
                        .value(1))
                .andExpect(jsonPath("$.page.totalPages")
                        .value(1))
                .andExpect(jsonPath("$.page.number")
                        .value(0));
    }

    @Test
    void shouldReturnEmptyListWithLinkWhenUserHasNoImages() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);
        Page<ImageResponse> emptyPage =
                new PageImpl<>(Collections.emptyList(), pageable, 0);


        when(imageService.getAllForOwner(eq(CURRENT_USER), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/images")
                        .with(user(CURRENT_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.imageResponseList")
                        .doesNotExist())
                .andExpect(jsonPath("$._links.self.href")
                        .value(endsWith("/api/images?page=0&size=2"))
                )
                .andExpect(jsonPath("$.page.size")
                        .value(2))
                .andExpect(jsonPath("$.page.totalElements")
                        .value(0))
                .andExpect(jsonPath("$.page.totalPages")
                        .value(0))
                .andExpect(jsonPath("$.page.number")
                        .value(0));
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
        when(imageService.getImageDataAsBytes(imageId, CURRENT_USER))
                .thenReturn(imageBytes);

        mockMvc.perform(get("/api/images/" + imageId)
                        .with(user(CURRENT_USER)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
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
    void shouldReturn200AndTransformedImageBytesAfterSingleTransformation() throws Exception {
        ImageTransformationService.TransformedImage transformedImage =
                new ImageTransformationService.TransformedImage(
                        "transformed-image-bytes".getBytes(), "png"
                );

        TransformationRequest transformationRequest = new FlipRequest();
        TransformImageRequest transformImageRequest = new TransformImageRequest(
                List.of(transformationRequest)
        );

        when(transformationService.transformImage(
                1L, CURRENT_USER, List.of(transformationRequest)
        )).thenReturn(transformedImage);

        mockMvc.perform(post("/api/images/" + IMAGE_ID + "/transform")
                        .with(user(CURRENT_USER))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transformImageRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(transformedImage.imageData()));
    }

    @Test
    void shouldReturn200AndTransformedImageBytesAfterMultipleTransformations() throws Exception {
        ImageTransformationService.TransformedImage transformedImage =
                new ImageTransformationService.TransformedImage(
                        "transformed-image-bytes".getBytes(), "png"
                );
        when(transformationService.transformImage(
                eq(IMAGE_ID),
                eq(CURRENT_USER),
                anyList())
        ).thenReturn(transformedImage);

        TransformImageRequest transformImageRequest = new TransformImageRequest(
                List.of(
                        new FlipRequest(),
                        new SepiaRequest(),
                        new MirrorRequest(),
                        new RotateRequest(180)
                )
        );

        mockMvc.perform(post("/api/images/{id}/transform", IMAGE_ID)
                        .with(user(CURRENT_USER))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transformImageRequest)))
                .andExpect(status().isOk());

        ArgumentCaptor<List<TransformationRequest>> captor =
                ArgumentCaptor.captor();

        verify(transformationService).transformImage(
                eq(IMAGE_ID),
                eq(CURRENT_USER),
                captor.capture()
        );

        List<TransformationRequest> transformations = captor.getValue();
        assertEquals(4, transformations.size());
        assertInstanceOf(FlipRequest.class, transformations.get(0));
        assertInstanceOf(SepiaRequest.class, transformations.get(1));
        assertInstanceOf(MirrorRequest.class, transformations.get(2));
        assertInstanceOf(RotateRequest.class, transformations.get(3));

        assertEquals(180, ((RotateRequest) transformations.get(3)).angle());
    }

    @Test
    void shouldReturn400WhenTransformationsListIsEmpty() throws Exception {
        TransformImageRequest request = new TransformImageRequest(List.of());

        mockMvc.perform(post("/api/images/{id}/transform", IMAGE_ID)
                        .with(user(CURRENT_USER))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(CURRENT_USER)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transformationService);
    }

    @Test
    void shouldReturn400ForUnknownTransformationType() throws Exception {
        String requestBody = """
                { "transformations": [ { "type": "BLUR" } ] }
                """;

        mockMvc.perform(post("/api/images/{id}/transform", IMAGE_ID)
                        .with(user(CURRENT_USER))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user(CURRENT_USER)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transformationService);
    }

    @Test
    void shouldReturn400WhenRequiredFieldIsMissingInTransformation() throws Exception {
        String requestBody = """
                { "transformations": [ { "type": "RESIZE", "width": 800 } ] }
                """;

        mockMvc.perform(post("/api/images/{id}/transform", IMAGE_ID)
                        .with(user(CURRENT_USER))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user(CURRENT_USER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenFieldValueViolatesConstraint() throws Exception {
        String requestBody = """
                { "transformations": [ { "type": "RESIZE", "width": -100, "height": 600 } ] }
                """;

        mockMvc.perform(post("/api/images/{id}/transform", IMAGE_ID)
                        .with(user(CURRENT_USER))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user(CURRENT_USER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/images/{id}/transform", IMAGE_ID)
                        .with(user(CURRENT_USER))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json ")
                        .with(user(CURRENT_USER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNoContentAfterSuccessfulDelete() throws Exception {
        mockMvc.perform(delete("/api/images/" + IMAGE_ID)
                        .with(user(CURRENT_USER))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonOwnedImage() throws Exception {
        doThrow(new ImageNotFoundException(IMAGE_ID))
                .when(imageService).delete(IMAGE_ID, CURRENT_USER);

        mockMvc.perform(delete("/api/images/" + IMAGE_ID)
                        .with(user(CURRENT_USER))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}