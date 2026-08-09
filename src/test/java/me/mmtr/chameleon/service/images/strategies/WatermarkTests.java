package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.WatermarkRequest;
import me.mmtr.chameleon.service.images.utilities.TestImageFactory;
import net.coobird.thumbnailator.geometry.Positions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WatermarkTests {

    private final Watermark watermark = new Watermark();

    @Test
    void shouldPreserveOriginalDimensionsAfterWatermark() {
        BufferedImage image =
                TestImageFactory.solidColorImage(
                        200,
                        100,
                        Color.BLUE);
        TransformationContext context = new TransformationContext(image);

        watermark.applyTransformation(
                new WatermarkRequest(
                        "test",
                        0.5f,
                        Positions.BOTTOM_RIGHT
                ), context);

        assertEquals(200, context.getImage().getWidth());
        assertEquals(100, context.getImage().getHeight());
    }

    @Test
    void shouldModifyPixesOnWatermarkPosition() {
        BufferedImage image =
                TestImageFactory.solidColorImage(
                        200,
                        100,
                        Color.BLACK);
        TransformationContext context = new TransformationContext(image);

        watermark.applyTransformation(
                new WatermarkRequest(
                        "WATERMARK",
                        0.5f,
                        Positions.CENTER
                ), context);

        BufferedImage watermarkedImage = context.getImage();

        int[] pixels = watermarkedImage.getRGB(
                0,
                0,
                watermarkedImage.getWidth(),
                watermarkedImage.getHeight(),
                null,
                0,
                watermarkedImage.getWidth()
        );

        boolean anyPixelChanged = false;
        for (int rgb : pixels) {
            Color pixelColor = new Color(rgb);
            if (!pixelColor.equals(Color.BLACK)) {
                anyPixelChanged = true;
                break;
            }
        }

        assertTrue(anyPixelChanged);
    }
}
