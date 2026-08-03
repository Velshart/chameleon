package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.TransformationRequest;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public interface ImageTransformationStrategy<T extends TransformationRequest> {
    Class<T> supportedType();
    void applyTransformation(T request, TransformationContext context);

    default BufferedImage applyTransform(
            BufferedImage image,
            AffineTransform transform
    ) {
        AffineTransformOp op = new AffineTransformOp(
                transform,
                AffineTransformOp.TYPE_BILINEAR
        );
        return op.filter(image, null);
    }
}
