package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.ResizeRequest;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public class Resize implements ImageTransformationStrategy<ResizeRequest> {
    @Override
    public Class<ResizeRequest> supportedType() {
        return ResizeRequest.class;
    }

    @Override
    public void applyTransformation(
            ResizeRequest request,
            TransformationContext context
    ) {
        try {
            BufferedImage resizedImage = Thumbnails.of(context.getImage())
                    .size(request.width(), request.height())
                    .keepAspectRatio(false)
                    .asBufferedImage();

            context.setImage(resizedImage);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to resize provided image", e
            );
        }
    }
}
