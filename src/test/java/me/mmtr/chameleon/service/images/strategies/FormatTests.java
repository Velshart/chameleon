package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.FormatRequest;
import me.mmtr.chameleon.service.images.utilities.TestImageFactory;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatTests {

    private final Format format = new Format();

    @Test
    void shouldSetOutputFormatOnContext() {
        BufferedImage image =
                TestImageFactory.solidColorImage(
                        100,
                        100,
                        Color.RED
                );

        TransformationContext context = new TransformationContext(image);
        FormatRequest request = new FormatRequest("jpg");

        format.applyTransformation(
                request,
                context
        );

        assertEquals("jpg", context.getOutputFormat());
    }
}
