package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.CropRequest;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public class Crop implements ImageTransformationStrategy<CropRequest> {
    @Override
    public Class<CropRequest> supportedType() {
        return CropRequest.class;
    }

    @Override
    public void applyTransformation(
            CropRequest request,
            TransformationContext context
    ) {
        try {
            BufferedImage croppedImage = Thumbnails.of(context.getImage())
                    .sourceRegion(
                            request.x(),
                            request.y(),
                            request.width(),
                            request.height()
                    )
                    .scale(1.0)
                    .asBufferedImage();

            context.setImage(croppedImage);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to crop provided image", e);
        }
    }
}
