package me.mmtr.chameleon.service.registry;

import me.mmtr.chameleon.exception.UnsupportedTransformationException;
import me.mmtr.chameleon.model.context.TransformationContext;
import me.mmtr.chameleon.model.dto.request.transformation.GrayscaleRequest;
import me.mmtr.chameleon.model.dto.request.transformation.ResizeRequest;
import me.mmtr.chameleon.service.images.registry.ImageTransformationStrategyRegistry;
import me.mmtr.chameleon.service.images.strategies.ImageTransformationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ImageTransformationStrategyRegistryTests {

    @Mock
    private ImageTransformationStrategy<ResizeRequest> resizeStrategy;

    @Mock
    private ImageTransformationStrategy<GrayscaleRequest> grayscaleStrategy;

    private TransformationContext transformationContext;

    @BeforeEach
    void setUp() {
        transformationContext = new TransformationContext(new BufferedImage(
                10, 10, BufferedImage.TYPE_INT_RGB
        ));
    }

    @Test
    void shouldBuildRegistryMappingEachStrategyToItsSupportedType() {
        when(resizeStrategy.supportedType())
                .thenReturn(ResizeRequest.class);
        when(grayscaleStrategy.supportedType())
                .thenReturn(GrayscaleRequest.class);


        ImageTransformationStrategyRegistry registry = new ImageTransformationStrategyRegistry(
                List.of(resizeStrategy, grayscaleStrategy)
        );

        var strategies = registry.getTransformationStrategies();
        assertEquals(2, strategies.size());
        assertEquals(resizeStrategy, strategies.get(ResizeRequest.class));
        assertEquals(grayscaleStrategy, strategies.get(GrayscaleRequest.class));
    }

    @Test
    void shouldDelegateToCorrectStrategyBasingOnRequestType() {
        when(resizeStrategy.supportedType())
                .thenReturn(ResizeRequest.class);
        when(grayscaleStrategy.supportedType())
                .thenReturn(GrayscaleRequest.class);

        ImageTransformationStrategyRegistry registry =
                new ImageTransformationStrategyRegistry(
                        List.of(resizeStrategy, grayscaleStrategy)
                );

        ResizeRequest resizeRequest = new ResizeRequest(5, 5);
        registry.apply(transformationContext, resizeRequest);

        verify(resizeStrategy).applyTransformation(
                resizeRequest,
                transformationContext
        );

        verifyNoMoreInteractions(grayscaleStrategy);
    }

    @Test
    void shouldThrowUnsupportedTransformationExceptionForNotRegisteredStrategy() {
        when(resizeStrategy.supportedType()).thenReturn(ResizeRequest.class);

        ImageTransformationStrategyRegistry registry =
                new ImageTransformationStrategyRegistry(List.of(resizeStrategy));

        GrayscaleRequest grayscaleRequest = new GrayscaleRequest();

        assertThrows(UnsupportedTransformationException.class, () ->
                registry.apply(transformationContext, grayscaleRequest)
        );

        verifyNoMoreInteractions(resizeStrategy);
    }
}
