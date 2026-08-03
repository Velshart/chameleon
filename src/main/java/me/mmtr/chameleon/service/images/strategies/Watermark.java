package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.WatermarkRequest;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public class Watermark implements ImageTransformationStrategy<WatermarkRequest> {
    @Override
    public Class<WatermarkRequest> supportedType() {
        return WatermarkRequest.class;
    }

    @Override
    public void applyTransformation(
            WatermarkRequest request,
            TransformationContext context
    ) {
        BufferedImage source = context.getImage();

        try {
            BufferedImage textWatermark = createTextWatermark(
                    request.text(),
                    source.getHeight()
            );

            BufferedImage result = Thumbnails.of(source)
                    .scale(1.0)
                    .watermark(
                            request.position(),
                            textWatermark,
                            request.opacity()
                    )
                    .asBufferedImage();

            context.setImage(result);

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to apply watermark transformation", e
            );
        }
    }

    private BufferedImage createTextWatermark(String text, int imageHeight) {
        Font font = new Font(
                "Arial",
                Font.BOLD,
                Math.max(12, imageHeight / 15)
        );

        BufferedImage temp = new BufferedImage(
                1,
                1,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = temp.createGraphics();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int width = fm.stringWidth(text);
        int height = fm.getHeight();
        g.dispose();

        if (width <= 0 || height <= 0) {
            return new BufferedImage(
                    1,
                    1,
                    BufferedImage.TYPE_INT_ARGB
            );
        }

        BufferedImage textImage = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB
        );
        g = textImage.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString(text, 0, fm.getAscent());
        g.dispose();

        return textImage;
    }
}
