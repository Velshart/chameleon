package me.mmtr.chameleon.service.images;

import me.mmtr.chameleon.exception.ImageNotFoundException;
import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.dto.request.ImageResponse;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.model.images.ImageData;
import me.mmtr.chameleon.repository.ImageDataRepository;
import me.mmtr.chameleon.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
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
    @Mock
    private ImageDataCache imageDataCache;
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

        ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.captor();

        verify(imageRepository).save(imageCaptor.capture());

        Image capturedImage = imageCaptor.getValue();

        assertEquals("cat.jpg", capturedImage.getOriginalFilename());
        assertEquals(owner, capturedImage.getOwner());

        ArgumentCaptor<ImageData> imageDataCaptor =
                ArgumentCaptor.captor();

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
    void shouldReturnImageDataAsBytesWhenOwnedByUser() {
        Image image = new Image(
                "cat.jpg",
                "image/jpeg",
                11L,
                owner
        );
        byte[] imageBytes = "image-bytes".getBytes();

        when(imageRepository.findByIdAndOwnerUsername(IMAGE_ID, USERNAME))
                .thenReturn(Optional.of(image));

        when(imageDataCache.getCachedData(IMAGE_ID)).thenReturn(
                imageBytes
        );

        byte[] data = imageService.getImageDataAsBytes(IMAGE_ID, owner);

        assertArrayEquals(imageBytes, data);
    }

    @Test
    void shouldThrowNotFoundWhenOwnershipCheckFails() {
        when(imageRepository.findByIdAndOwnerUsername(IMAGE_ID, USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(
                ImageNotFoundException.class,
                () -> imageService.getImageDataAsBytes(IMAGE_ID, owner)
        );

        verifyNoInteractions(imageDataCache);
    }

    @Test
    void shouldPropagateImageNotFoundExceptionWhenImageDataIsMissing() {
        Image image = new Image(
                "cat.jpg",
                "image/jpeg",
                11L,
                owner
        );

        when(imageRepository.findByIdAndOwnerUsername(IMAGE_ID, USERNAME))
                .thenReturn(Optional.of(image));
        when(imageDataCache.getCachedData(IMAGE_ID)).thenThrow(
                new ImageNotFoundException(IMAGE_ID)
        );

        assertThrows(
                ImageNotFoundException.class,
                () -> imageService.getImageDataAsBytes(IMAGE_ID, owner)
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
        verify(imageDataCache).evict(IMAGE_ID);
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

        Pageable pageable = PageRequest.of(0, 2);
        Page<Image> imagePage = new PageImpl<>(List.of(image1, image2), pageable, 2);

        when(imageRepository.findAllByOwnerUsername(
                USERNAME,
                pageable
        )).thenReturn(imagePage);

        Page<ImageResponse> result = imageService.getAllForOwner(
                owner,
                pageable
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        assertEquals(
                image1.getOriginalFilename(),
                result.getContent().get(0).originalFilename()
        );
        assertEquals(
                image2.getOriginalFilename(),
                result.getContent().get(1).originalFilename()
        );

        verify(imageRepository)
                .findAllByOwnerUsername(
                        USERNAME,
                        pageable
                );
    }

    @Test
    void shouldReturnEmptyListWhenOwnerHasNoImages() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Image> page = new PageImpl<>(
                Collections.emptyList(),
                pageable,
                0
        );
        when(imageRepository.findAllByOwnerUsername(
                USERNAME,
                pageable
        )).thenReturn(page);

        Page<ImageResponse> result =
                imageService.getAllForOwner(owner, pageable);

        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.getContent().isEmpty());

        verify(imageRepository)
                .findAllByOwnerUsername(
                        USERNAME,
                        pageable
                );
    }
}
