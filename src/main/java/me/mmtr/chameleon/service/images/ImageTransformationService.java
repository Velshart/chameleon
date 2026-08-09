package me.mmtr.chameleon.service.images;

import me.mmtr.chameleon.exception.ImageNotFoundException;
import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.TransformationRequest;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.model.images.ImageData;
import me.mmtr.chameleon.repository.ImageDataRepository;
import me.mmtr.chameleon.repository.ImageRepository;
import me.mmtr.chameleon.service.images.registry.ImageTransformationStrategyRegistry;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class ImageTransformationService {
    private final ImageService imageService;
    private final ImageTransformationStrategyRegistry strategyRegistry;
    private final ImageRepository imageRepository;
    private final ImageDataRepository imageDataRepository;

    public ImageTransformationService(
            ImageService imageService,
            ImageTransformationStrategyRegistry strategyRegistry,
            ImageRepository imageRepository, ImageDataRepository imageDataRepository) {
        this.imageService = imageService;
        this.strategyRegistry = strategyRegistry;
        this.imageRepository = imageRepository;
        this.imageDataRepository = imageDataRepository;
    }

    @CacheEvict(value = "image_data", key = "#id")
    public TransformedImage transformImage(
            Long id,
            User currentUser,
            List<TransformationRequest> transformations
    ) {
        Image databaseImage = imageService.getImageForOwner(id, currentUser);

        byte[] databaseImageBytes = imageService.getImageDataAsBytes(id, currentUser);

        BufferedImage decodedImage = decode(databaseImageBytes);

        TransformationContext context = new TransformationContext(decodedImage);
        for (TransformationRequest request : transformations) {
            strategyRegistry.apply(context, request);
        }

        byte[] resultBytes = encode(context);
        String newContentType = "image/" + context.getOutputFormat();

        ImageData imageData = imageDataRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException(id));

        imageData.setData(resultBytes);
        imageDataRepository.save(imageData);

        databaseImage.updateAfterTransformation(newContentType, resultBytes.length);
        imageRepository.save(databaseImage);

        return new TransformedImage(
                resultBytes,
                context.getOutputFormat()
        );
    }


    private BufferedImage decode(byte[] data) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            if (image == null) {
                throw new IllegalStateException("Invalid image format");
            }
            return image;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read image", e);
        }
    }

    private byte[] encode(TransformationContext context) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(context.getImage())
                    .scale(1.0)
                    .outputFormat(context.getOutputFormat())
                    .outputQuality(context.getQuality())
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to encode transformed image", e);
        }
    }

    public record TransformedImage(byte[] imageData, String format) {
    }
}
