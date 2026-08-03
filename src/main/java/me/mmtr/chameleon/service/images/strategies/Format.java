package me.mmtr.chameleon.service.images.strategies;

import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.FormatRequest;
import org.springframework.stereotype.Component;

@Component
public class Format implements ImageTransformationStrategy<FormatRequest> {
    @Override
    public Class<FormatRequest> supportedType() {
        return FormatRequest.class;
    }

    @Override
    public void applyTransformation(
            FormatRequest request,
            TransformationContext context
    ) {
        context.setOutputFormat(request.format().toLowerCase());
    }
}
