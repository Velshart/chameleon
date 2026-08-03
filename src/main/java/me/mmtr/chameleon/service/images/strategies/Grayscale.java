package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.GrayscaleRequest;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;

@Component
public class Grayscale implements ImageTransformationStrategy<GrayscaleRequest> {
    @Override
    public Class<GrayscaleRequest> supportedType() {
        return GrayscaleRequest.class;
    }

    @Override
    public void applyTransformation(
            GrayscaleRequest request,
            TransformationContext context
    ) {
        BufferedImage originalImage = context.getImage();

        BufferedImage grayImage =
                new BufferedImage(
                        originalImage.getWidth(),
                        originalImage.getHeight(),
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D g = grayImage.createGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();

        context.setImage(grayImage);
    }
}
