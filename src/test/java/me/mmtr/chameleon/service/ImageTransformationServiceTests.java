package me.mmtr.chameleon.service;

import me.mmtr.chameleon.exception.ImageNotFoundException;
import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.GrayscaleRequest;
import me.mmtr.chameleon.model.dto.request.transformation.ResizeRequest;
import me.mmtr.chameleon.model.dto.request.transformation.TransformationRequest;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.model.images.ImageData;
import me.mmtr.chameleon.repository.ImageDataRepository;
import me.mmtr.chameleon.repository.ImageRepository;
import me.mmtr.chameleon.service.images.ImageService;
import me.mmtr.chameleon.service.images.ImageTransformationService;
import me.mmtr.chameleon.service.images.registry.ImageTransformationStrategyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageTransformationServiceTests {

    private static final Long IMAGE_ID = 1L;
    private static final String USERNAME = "test_user";
    @Mock
    private ImageService imageService;
    @Mock
    private ImageTransformationStrategyRegistry strategyRegistry;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ImageDataRepository imageDataRepository;
    @InjectMocks
    private ImageTransformationService transformationService;
    private User owner;
    private byte[] validPngBytes;

    @BeforeEach
    void setUp() throws IOException {
        owner = new User(USERNAME, "password");
        validPngBytes = createTestPng();
    }

    @Test
    void shouldTransformImageAndSaveChanges() {
        Image image = new Image(
                "cat.png",
                "image/png",
                validPngBytes.length,
                owner
        );
        ImageData imageData = new ImageData(image, validPngBytes);

        when(imageService.getImageForOwner(IMAGE_ID, owner))
                .thenReturn(image);
        when(imageService.getImageData(IMAGE_ID, owner))
                .thenReturn(imageData);

        List<TransformationRequest> transformations = List.of(new GrayscaleRequest());

        ImageTransformationService.TransformedImage result =
                transformationService.transformImage(IMAGE_ID, owner, transformations);

        assertNotNull(result.imageData());
        assertEquals("png", result.format());

        verify(imageDataRepository).save(imageData);
        verify(imageRepository).save(image);
    }

    @Test
    void shouldApplyEachTransformationInOrder() {
        Image image = new Image(
                "cat.png",
                "image/png",
                validPngBytes.length,
                owner
        );
        ImageData imageData = new ImageData(image, validPngBytes);

        when(imageService.getImageForOwner(IMAGE_ID, owner)).thenReturn(image);
        when(imageService.getImageData(IMAGE_ID, owner))
                .thenReturn(imageData);

        List<TransformationRequest> transformations = List.of(
                new GrayscaleRequest(),
                new ResizeRequest(5, 5)
        );

        transformationService.transformImage(
                IMAGE_ID, owner, transformations
        );

        ArgumentCaptor<TransformationRequest> requestArgumentCaptor =
                ArgumentCaptor.forClass(TransformationRequest.class);

        verify(strategyRegistry, times(2))
                .apply(
                        any(TransformationContext.class),
                        requestArgumentCaptor.capture()
                );

        List<TransformationRequest> capturedRequests =
                requestArgumentCaptor.getAllValues();

        assertEquals(transformations, capturedRequests);
        assertEquals(2, capturedRequests.size());
    }

    @Test
    void shouldPropagateImageNotFoundExceptionWhenOwnershipCheckFails() {
        when(imageService.getImageForOwner(IMAGE_ID, owner))
                .thenThrow(new ImageNotFoundException(IMAGE_ID));

        assertThrows(ImageNotFoundException.class, () ->
                transformationService.transformImage(
                        IMAGE_ID,
                        owner,
                        List.of()
                ));
    }

    @Test
    void shouldPropagateImageNotFoundExceptionWhenImageDataIsMissing() {
        Image image = new Image(
                "cat.png",
                "image/png",
                validPngBytes.length,
                owner
        );

        when(imageService.getImageForOwner(IMAGE_ID, owner)).thenReturn(image);
        when(imageService.getImageData(IMAGE_ID, owner))
                .thenThrow(new ImageNotFoundException(IMAGE_ID));

        assertThrows(ImageNotFoundException.class, () ->
                transformationService.transformImage(
                        IMAGE_ID,
                        owner,
                        List.of()
                ));
    }

    @Test
    void shouldThrowIllegalStateExceptionForInvalidImageBytes() {
        byte[] invalidPngBytes = "definitely not an image".getBytes();

        Image image = new Image(
                "cat.png",
                "image/png",
                invalidPngBytes.length,
                owner
        );

        when(imageService.getImageForOwner(IMAGE_ID, owner)).thenReturn(image);

        ImageData imageData = new ImageData(image, invalidPngBytes);
        when(imageService.getImageData(IMAGE_ID, owner)).thenReturn(imageData);

        assertThrows(IllegalStateException.class, () ->
                transformationService.transformImage(
                        IMAGE_ID,
                        owner,
                        List.of()
                )
        );
    }

    private byte[] createTestPng() throws IOException {
        BufferedImage image = new BufferedImage(
                10,
                10,
                BufferedImage.TYPE_INT_RGB
        );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
