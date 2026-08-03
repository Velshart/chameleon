package me.mmtr.chameleon.service.images;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.CropRequest;
import me.mmtr.chameleon.service.images.strategies.Crop;
import me.mmtr.chameleon.service.images.utilities.TestImageFactory;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CropTests {

    private final Crop crop = new Crop();

    @Test
    void shouldCropToRequestedDimensions() {
        BufferedImage image =
                TestImageFactory.solidColorImage(
                        100,
                        100,
                        Color.RED
                );

        TransformationContext context = new TransformationContext(image);
        CropRequest request = new CropRequest(
                50,
                50,
                50,
                50
        );

        crop.applyTransformation(
                request,
                context
        );

        assertEquals(50, context.getImage().getWidth());
        assertEquals(50, context.getImage().getHeight());
    }

    @Test
    void shouldPreserveColorFromCroppedRegion() {
        BufferedImage image =
                TestImageFactory.cornerMarkedImage(100, 100);

        TransformationContext context = new TransformationContext(image);

        crop.applyTransformation(new CropRequest(
                0,
                0,
                25,
                25
        ), context);

        int pixel = context.getImage().getRGB(0, 0);
        assertEquals(Color.RED, new Color(pixel));
    }
}