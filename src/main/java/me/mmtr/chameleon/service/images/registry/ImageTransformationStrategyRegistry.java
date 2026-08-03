package me.mmtr.chameleon.service.images.registry;

import me.mmtr.chameleon.exception.UnsupportedTransformationException;
import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.TransformationRequest;
import me.mmtr.chameleon.service.images.strategies.ImageTransformationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ImageTransformationStrategyRegistry {

    private final Map<Class<? extends TransformationRequest>, ImageTransformationStrategy<? extends TransformationRequest>> transformationStrategies;

    public ImageTransformationStrategyRegistry(
            List<ImageTransformationStrategy<? extends TransformationRequest>> strategiesList
    ) {
        this.transformationStrategies = strategiesList.stream()
                .collect(Collectors.toMap(
                        ImageTransformationStrategy::supportedType,
                        Function.identity()
                ));
    }

    @SuppressWarnings("unchecked")
    public <T extends TransformationRequest> void apply(
            TransformationContext context,
            T request
    ) {
        ImageTransformationStrategy<T> strategy =
                (ImageTransformationStrategy<T>) transformationStrategies
                        .get(request.getClass());

        if (strategy == null) {
            throw new UnsupportedTransformationException(request.getClass());
        }

        strategy.applyTransformation(request, context);
    }

    public Map<
            Class<? extends TransformationRequest>,
            ImageTransformationStrategy<? extends TransformationRequest>
            > getTransformationStrategies() {
        return transformationStrategies;
    }
}
