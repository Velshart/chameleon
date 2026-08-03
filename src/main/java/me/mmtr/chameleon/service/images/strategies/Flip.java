package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.FlipRequest;
import org.springframework.stereotype.Component;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

@Component
public class Flip implements ImageTransformationStrategy<FlipRequest> {
    @Override
    public Class<FlipRequest> supportedType() {
        return FlipRequest.class;
    }

    @Override
    public void applyTransformation(
            FlipRequest request,
            TransformationContext context
    ) {
        BufferedImage image = context.getImage();
        AffineTransform transform = AffineTransform.getScaleInstance(
                1,
                -1
        );
        transform.translate(0, -image.getHeight());
        context.setImage(applyTransform(image, transform));
    }
}
