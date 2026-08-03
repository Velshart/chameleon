package me.mmtr.chameleon.service.images;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.ResizeRequest;
import me.mmtr.chameleon.service.images.strategies.Resize;
import me.mmtr.chameleon.service.images.utilities.TestImageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResizeTests {

    private final Resize resize = new Resize();

    private TransformationContext context;

    @BeforeEach
    void setUp() {
        BufferedImage image = TestImageFactory.solidColorImage(
                10,
                10,
                Color.RED
        );

        context = new TransformationContext(image);
    }


    @Test
    void shouldResizeImageToLargerDimensions() {
        resize.applyTransformation(
                new ResizeRequest(100, 100), context
        );

        assertEquals(100, context.getImage().getWidth());
        assertEquals(100, context.getImage().getHeight());
    }

    @Test
    void shouldResizeImageToSmallerDimensions() {
        resize.applyTransformation(
                new ResizeRequest(5, 5), context
        );

        assertEquals(5, context.getImage().getWidth());
        assertEquals(5, context.getImage().getHeight());
    }
}
