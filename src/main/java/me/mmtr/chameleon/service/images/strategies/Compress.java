package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.CompressRequest;
import org.springframework.stereotype.Component;

@Component
public class Compress implements ImageTransformationStrategy<CompressRequest> {
    @Override
    public Class<CompressRequest> supportedType() {
        return CompressRequest.class;
    }

    @Override
    public void applyTransformation(
            CompressRequest request,
            TransformationContext context
    ) {
        context.setQuality(request.quality());
    }
}
