package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.SepiaRequest;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

@Component
public class Sepia implements ImageTransformationStrategy<SepiaRequest> {
    @Override
    public Class<SepiaRequest> supportedType() {
        return SepiaRequest.class;
    }

    @Override
    public void applyTransformation(
            SepiaRequest request,
            TransformationContext context
    ) {
        BufferedImage source = context.getImage();
        int width = source.getWidth();
        int height = source.getHeight();

        int[] pixels = new int[width * height];
        source.getRGB(0, 0, width, height, pixels, 0, width);

        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];

            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            int newR = Math.min(255, (int) (0.393 * r + 0.769 * g + 0.189 * b));
            int newG = Math.min(255, (int) (0.349 * r + 0.686 * g + 0.168 * b));
            int newB = Math.min(255, (int) (0.272 * r + 0.534 * g + 0.131 * b));

            pixels[i] = (rgb & 0xFF000000) | (newR << 16) | (newG << 8) | newB;
        }

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, width, height, pixels, 0, width);

        context.setImage(result);
    }
}
