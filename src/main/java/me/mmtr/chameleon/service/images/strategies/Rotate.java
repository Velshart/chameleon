package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.RotateRequest;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public class Rotate implements ImageTransformationStrategy<RotateRequest> {
    @Override
    public Class<RotateRequest> supportedType() {
        return RotateRequest.class;
    }

    @Override
    public void applyTransformation(
            RotateRequest request,
            TransformationContext context
    ) {
        try {
            BufferedImage rotatedImage = Thumbnails.of(context.getImage())
                    .scale(1.0)
                    .rotate(request.angle())
                    .asBufferedImage();

            context.setImage(rotatedImage);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to rotate provided image", e);
        }
    }
}
