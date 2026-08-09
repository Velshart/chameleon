package me.mmtr.chameleon.service.images;

import me.mmtr.chameleon.exception.ImageNotFoundException;
import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.model.images.ImageData;
import me.mmtr.chameleon.repository.ImageDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageDataCacheTests {

    @Mock
    private ImageDataRepository imageDataRepository;

    @InjectMocks
    private ImageDataCache imageDataCache;

    private static final Long IMAGE_ID = 1L;

    @Test
    void shouldReturnImageBytesWhenImageDataExists() {
        User owner = new User("test_user", "password");
        Image image = new Image(
                "cat.png",
                "image/png",
                100L,
                owner
        );
        byte[] imageBytes = "image-bytes".getBytes();
        ImageData imageData = new ImageData(image, imageBytes);

        when(imageDataRepository.findById(IMAGE_ID))
                .thenReturn(Optional.of(imageData));

        byte[] result = imageDataCache.getCachedData(IMAGE_ID);

        assertArrayEquals(imageBytes, result);
    }

    @Test
    void shouldThrowImageNotFoundExceptionWhenDataDoesNotExist() {
        when(imageDataRepository.findById(IMAGE_ID))
                .thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class, () ->
                imageDataCache.getCachedData(IMAGE_ID)
        );
    }
}
