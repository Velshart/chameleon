package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.CompressRequest;
import me.mmtr.chameleon.service.images.utilities.TestImageFactory;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompressTests {

    private final Compress compress = new Compress();

    @Test
    void shouldSetQualityOnTransformationContext() {
        BufferedImage original =
                TestImageFactory.solidColorImage(
                        10,
                        10,
                        Color.RED
                );

        TransformationContext context = new TransformationContext(original);
        CompressRequest request = new CompressRequest(0.5f);
        compress.applyTransformation(
                request,
                context
        );

        assertEquals(0.5f, context.getQuality());
    }
}
