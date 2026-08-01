package me.mmtr.chameleon.service;

import me.mmtr.chameleon.exception.ImageNotFoundException;
import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.dto.request.ImageResponse;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.model.images.ImageData;
import me.mmtr.chameleon.repository.ImageDataRepository;
import me.mmtr.chameleon.repository.ImageRepository;
import me.mmtr.chameleon.service.images.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    private static final String USERNAME = "test_user";
    private static final Long IMAGE_ID = 1L;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ImageDataRepository imageDataRepository;
    @InjectMocks
    private ImageService imageService;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User(USERNAME, "password");
    }

    @Test
    void shouldUploadAndReturnResponse() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cat.jpg",
                "image/jpeg",
                "image-bytes".getBytes()
        );

        Image savedImage = new Image(
                "cat.jpg",
                "image/jpeg",
                11L,
                owner
        );

        when(imageRepository.save(any(Image.class)))
                .thenReturn(savedImage);

        ImageResponse imageResponse = imageService.upload(file, owner);

        assertEquals("cat.jpg", imageResponse.originalFilename());
        assertEquals("image/jpeg", imageResponse.contentType());

        ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);

        verify(imageRepository).save(imageCaptor.capture());

        Image capturedImage = imageCaptor.getValue();

        assertEquals("cat.jpg", capturedImage.getOriginalFilename());
        assertEquals(owner, capturedImage.getOwner());

        ArgumentCaptor<ImageData> imageDataCaptor =
                ArgumentCaptor.forClass(ImageData.class);

        verify(imageDataRepository).save(imageDataCaptor.capture());

        ImageData capturedImagedata = imageDataCaptor.getValue();

        assertArrayEquals("image-bytes".getBytes(), capturedImagedata.getData());
    }

    @Test
    void shouldWrapIOExceptionInUncheckedIOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("cat.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(imageRepository.save(any(Image.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );
        when(file.getBytes()).thenThrow(new IOException("exception"));

        assertThrows(
                UncheckedIOException.class,
                () -> imageService.upload(file, owner)
        );

        verify(imageDataRepository, never()).save(any());
    }

    @Test
    void shouldReturnImageWhenOwnedByUser() {
        Image image = new Image(
                "cat.jpg",
                "image/jpeg",
                11L,
                owner
        );

        when(imageRepository.findByIdAndOwnerUsername(IMAGE_ID, USERNAME))
                .thenReturn(Optional.of(image));

        Image result = imageService.getImageForOwner(IMAGE_ID, owner);

        assertEquals(image, result);
    }

    @Test
    void shouldThrowNotFoundWhenImageDoesNotExist() {
        when(imageRepository.findByIdAndOwnerUsername(IMAGE_ID, USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(
                ImageNotFoundException.class,
                () -> imageService.getImageForOwner(IMAGE_ID, owner)
        );
    }

    @Test
    void shouldReturnImageDataWhenOwnedByUser() {
        Image image = new Image(
                "cat.jpg",
                "image/jpeg",
                11L,
                owner
        );
        ImageData imageData = new ImageData(image, "image-bytes".getBytes());

        when(imageRepository.findByIdAndOwnerUsername(IMAGE_ID, USERNAME))
                .thenReturn(Optional.of(image));
        when(imageDataRepository.findById(IMAGE_ID))
                .thenReturn(Optional.of(imageData));

        byte[] data = imageService.getImageData(IMAGE_ID, owner);

        assertArrayEquals("image-bytes".getBytes(), data);
    }

    @Test
    void shouldThrowNotFoundWhenOwnershipCheckFails() {
        when(imageRepository.findByIdAndOwnerUsername(IMAGE_ID, USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(
                ImageNotFoundException.class,
                () -> imageService.getImageData(IMAGE_ID, owner)
        );

        verify(imageDataRepository, never()).findById(anyLong());
    }

    @Test
    void shouldThrowNotFoundWhenImageDataNotFound() {
        Image image = new Image(
                "cat.jpg",
                "image/jpeg",
                11L,
                owner
        );

        when(imageRepository.findByIdAndOwnerUsername(IMAGE_ID, USERNAME))
                .thenReturn(Optional.of(image));
        when(imageDataRepository.findById(IMAGE_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ImageNotFoundException.class,
                () -> imageService.getImageData(IMAGE_ID, owner)
        );
    }

    @Test
    void shouldDeleteImageAndItsData() {
        Image image = new Image(
                "cat.jpg",
                "image/jpeg",
                11L,
                owner
        );
        ReflectionTestUtils.setField(image, "id", IMAGE_ID);

        when(imageRepository.findByIdAndOwnerUsername(IMAGE_ID, USERNAME))
                .thenReturn(Optional.of(image));

        imageService.delete(IMAGE_ID, owner);

        verify(imageDataRepository).deleteById(IMAGE_ID);
        verify(imageRepository).delete(image);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonOwnedImage() {
        when(imageRepository.findByIdAndOwnerUsername(IMAGE_ID, USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(
                ImageNotFoundException.class,
                () -> imageService.delete(IMAGE_ID, owner)
        );

        verify(imageDataRepository, never()).deleteById(any());
        verify(imageRepository, never()).delete(any());
    }

    @Test
    void shouldReturnAllImagesOwnedByUser() {
        Image image1 = new Image(
                "cat.jpg",
                "image/jpeg",
                11L,
                owner
        );
        Image image2 = new Image(
                "dog.png",
                "image/png",
                22L,
                owner
        );

        when(imageRepository.findAllByOwnerUsernameOrderByUploadedAtDesc(USERNAME))
                .thenReturn(List.of(image1, image2));

        List<ImageResponse> result = imageService.getAllForOwner(owner);

        assertEquals(2, result.size());
        assertEquals(
                image1.getOriginalFilename(),
                result.getFirst().originalFilename()
        );
        assertEquals(
                image2.getOriginalFilename(),
                result.getLast().originalFilename()
        );
    }

    @Test
    void shouldReturnEmptyListWhenOwnerHasNoImages() {
        when(imageRepository.findAllByOwnerUsernameOrderByUploadedAtDesc(USERNAME))
                .thenReturn(List.of());

        List<ImageResponse> result = imageService.getAllForOwner(owner);

        assertEquals(0, result.size());
    }
}
