package me.mmtr.chameleon.service.images;

import jakarta.transaction.Transactional;
import me.mmtr.chameleon.exception.ImageNotFoundException;
import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.dto.request.ImageResponse;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.model.images.ImageData;
import me.mmtr.chameleon.repository.ImageDataRepository;
import me.mmtr.chameleon.repository.ImageRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageDataRepository imageDataRepository;
    private final ImageDataCache imageDataCache;


    public ImageService(
            ImageRepository imageRepository,
            ImageDataRepository imageDataRepository,
            ImageDataCache imageDataCache
    ) {
        this.imageRepository = imageRepository;
        this.imageDataRepository = imageDataRepository;
        this.imageDataCache = imageDataCache;
    }

    @Transactional
    public ImageResponse upload(MultipartFile file, User owner) {
        try {
            Image image = new Image(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    owner
            );

            Image savedImage = imageRepository.save(image);

            ImageData imageData = new ImageData(
                    savedImage,
                    file.getBytes()
            );

            imageDataRepository.save(imageData);

            return ImageResponse.fromImage(savedImage);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to upload image", e);
        }
    }

    public Image getImageForOwner(Long id, User currentUser) {
        return imageRepository
                .findByIdAndOwnerUsername(id, currentUser.getUsername())
                .orElseThrow(() -> new ImageNotFoundException(id));
    }

    @Cacheable(value = "image_data", key = "#id")
    public byte[] getImageDataAsBytes(Long id, User currentUser) {
        getImageForOwner(id, currentUser);

        return imageDataCache.getCachedData(id);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Image image = getImageForOwner(id, currentUser);

        imageDataRepository.deleteById(image.getId());
        imageRepository.delete(image);
        imageDataCache.evict(id);
    }

    public Page<ImageResponse> getAllForOwner(
            User currentUser,
            Pageable pageable
    ) {
        return imageRepository.findAllByOwnerUsername(
                currentUser.getUsername(),
                pageable
        ).map(ImageResponse::fromImage);
    }
}
