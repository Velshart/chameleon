package me.mmtr.chameleon.service.images;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.RotateRequest;
import me.mmtr.chameleon.service.images.strategies.Rotate;
import me.mmtr.chameleon.service.images.utilities.TestImageFactory;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RotateTests {

    private final Rotate rotate = new Rotate();

    @Test
    void shouldSwapDimensionsAfter90DegreeRotation() {
        BufferedImage original = TestImageFactory.solidColorImage(
                100,
                50,
                Color.RED
        );
        TransformationContext context = new TransformationContext(original);

        rotate.applyTransformation(new RotateRequest(90), context);

        assertEquals(50, context.getImage().getWidth());
        assertEquals(100, context.getImage().getHeight());
    }

    @Test
    void shouldPreserveDimensionsAfter180DegreeRotation() {
        BufferedImage original = TestImageFactory.solidColorImage(
                100,
                50,
                Color.BLUE
        );
        TransformationContext context = new TransformationContext(original);

        rotate.applyTransformation(new RotateRequest(180), context);

        assertThat(context.getImage().getWidth()).isEqualTo(100);
        assertThat(context.getImage().getHeight()).isEqualTo(50);
    }

}
