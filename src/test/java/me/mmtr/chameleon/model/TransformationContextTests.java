package me.mmtr.chameleon.model;

import me.mmtr.chameleon.model.context.TransformationContext;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransformationContextTests {
    private final BufferedImage image = new BufferedImage(
            10,
            10,
            BufferedImage.TYPE_INT_RGB
    );

    private final TransformationContext context =
            new TransformationContext(image);

    @Test
    void shouldCorrectlySetUpValuesAfterConstruction() {
        assertEquals("png", context.getOutputFormat());
        assertEquals(1.0, context.getQuality());
        assertEquals(image, context.getImage());
    }
}
