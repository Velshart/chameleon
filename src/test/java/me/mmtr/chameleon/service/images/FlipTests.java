package me.mmtr.chameleon.service.images;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.FlipRequest;
import me.mmtr.chameleon.service.images.strategies.Flip;
import me.mmtr.chameleon.service.images.utilities.TestImageFactory;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlipTests {

    private final Flip flip = new Flip();

    @Test
    void shouldMoveTopLeftMarkerToBottomLeft() {
        BufferedImage image =
                TestImageFactory.cornerMarkedImage(100, 100);
        TransformationContext context = new TransformationContext(image);

        flip.applyTransformation(new FlipRequest(), context);

        Color bottomLeft = new Color(context.getImage().getRGB(0, 99));
        Color topLeft = new Color(context.getImage().getRGB(0, 0));

        assertEquals(Color.RED, bottomLeft);
        assertEquals(Color.WHITE, topLeft);
    }
}
