package me.mmtr.chameleon.service.images;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.SepiaRequest;
import me.mmtr.chameleon.service.images.strategies.Sepia;
import me.mmtr.chameleon.service.images.utilities.TestImageFactory;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SepiaTests {

    private final Sepia sepia = new Sepia();

    @Test
    void shouldTintWhitePixelsWarmAfterSepia() {
        BufferedImage image = TestImageFactory.solidColorImage(
                10,
                10,
                Color.WHITE
        );
        TransformationContext context = new TransformationContext(image);

        sepia.applyTransformation(new SepiaRequest(), context);

        Color result = new Color(context.getImage().getRGB(5, 5));

        assertTrue(result.getRed() >= result.getGreen());
        assertTrue(result.getGreen() >= result.getBlue());
    }
}
