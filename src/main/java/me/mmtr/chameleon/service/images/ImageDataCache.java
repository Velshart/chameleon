package me.mmtr.chameleon.service.images;

import me.mmtr.chameleon.exception.ImageNotFoundException;
import me.mmtr.chameleon.model.images.ImageData;
import me.mmtr.chameleon.repository.ImageDataRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class ImageDataCache {
    private final ImageDataRepository imageDataRepository;

    public ImageDataCache(ImageDataRepository imageDataRepository) {
        this.imageDataRepository = imageDataRepository;
    }

    @Cacheable(value = "image_data", key = "#id")
    public byte[] getCachedData(Long id) {
        return imageDataRepository.findById(id)
                .map(ImageData::getData)
                .orElseThrow(() -> new ImageNotFoundException(id));
    }

    @CacheEvict(value = "image_data", key = "#id")
    public void evict(Long id) {}
}
