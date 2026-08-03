package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.MirrorRequest;
import org.springframework.stereotype.Component;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

@Component
public class Mirror implements ImageTransformationStrategy<MirrorRequest> {
    @Override
    public Class<MirrorRequest> supportedType() {
        return MirrorRequest.class;
    }

    @Override
    public void applyTransformation(
            MirrorRequest request,
            TransformationContext context
    ) {
        BufferedImage image = context.getImage();
        AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
        transform.translate(-image.getWidth(), 0);
        context.setImage(applyTransform(image, transform));
    }
}
