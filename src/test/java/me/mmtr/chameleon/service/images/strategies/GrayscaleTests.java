package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.GrayscaleRequest;
import me.mmtr.chameleon.service.images.utilities.TestImageFactory;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GrayscaleTests {

    private final Grayscale grayscale = new Grayscale();

    @Test
    void shouldMakeAllColorChannelsEqual() {
        BufferedImage image =
                TestImageFactory.solidColorImage(
                        10,
                        10,
                        Color.RED
                );
        TransformationContext context = new TransformationContext(image);

        grayscale.applyTransformation(new GrayscaleRequest(), context);

        BufferedImage grayscaleImage = context.getImage();

        int[] pixels = grayscaleImage.getRGB(
                0,
                0,
                grayscaleImage.getWidth(),
                grayscaleImage.getHeight(),
                null,
                0,
                grayscaleImage.getWidth()
        );

        for (int rgb : pixels) {
            Color color = new Color(rgb);
            assertEquals(
                    color.getRed(),
                    color.getGreen()
            );
            assertEquals(
                    color.getGreen(),
                    color.getBlue()
            );
        }
    }
}
