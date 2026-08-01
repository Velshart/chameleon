package me.mmtr.chameleon.service.images;

import jakarta.transaction.Transactional;
import me.mmtr.chameleon.exception.ImageNotFoundException;
import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.dto.request.ImageResponse;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.model.images.ImageData;
import me.mmtr.chameleon.repository.ImageDataRepository;
import me.mmtr.chameleon.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageDataRepository imageDataRepository;


    public ImageService(
            ImageRepository imageRepository,
            ImageDataRepository imageDataRepository
    ) {
        this.imageRepository = imageRepository;
        this.imageDataRepository = imageDataRepository;
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
        return imageRepository.findByIdAndOwnerUsername(id, currentUser.getUsername())
                .orElseThrow(() -> new ImageNotFoundException(id));
    }

    public byte[] getImageData(Long id, User currentUser) {
        getImageForOwner(id, currentUser);

        return imageDataRepository.findById(id)
                .map(ImageData::getData)
                .orElseThrow(() -> new ImageNotFoundException(id));
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Image image = getImageForOwner(id, currentUser);

        imageDataRepository.deleteById(image.getId());
        imageRepository.delete(image);
    }

    public List<ImageResponse> getAllForOwner(User currentUser) {
        return imageRepository.findAllByOwnerUsernameOrderByUploadedAtDesc(currentUser.getUsername())
                .stream()
                .map(ImageResponse::fromImage)
                .toList();
    }
}
