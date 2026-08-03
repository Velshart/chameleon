package me.mmtr.chameleon.service.images;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.MirrorRequest;
import me.mmtr.chameleon.service.images.strategies.Mirror;
import me.mmtr.chameleon.service.images.utilities.TestImageFactory;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MirrorTests {

    private final Mirror mirror = new Mirror();

    @Test
    void shouldMoveTopLeftMarkerToTopRightAfterHorizontalMirror() {
        BufferedImage image =
                TestImageFactory.cornerMarkedImage(100, 100);
        TransformationContext context = new TransformationContext(image);

        mirror.applyTransformation(new MirrorRequest(), context);

        Color topRight = new Color(context.getImage().getRGB(99, 0));
        Color topLeft = new Color(context.getImage().getRGB(0, 0));

        assertEquals(Color.RED, topRight);
        assertEquals(Color.WHITE, topLeft);
    }
}
