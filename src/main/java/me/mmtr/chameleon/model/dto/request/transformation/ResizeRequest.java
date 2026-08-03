package me.mmtr.chameleon.model.dto.request.transformation;

import jakarta.validation.constraints.Positive;

public record ResizeRequest(
        @Positive(message = "Resize: width has to be positive") int width,
        @Positive(message = "Resize: height has to be positive") int height
) implements TransformationRequest {}
