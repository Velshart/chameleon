package me.mmtr.chameleon.model.dto.request.transformation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CropRequest(
        @PositiveOrZero(message = "Crop: x-value has to be positive") int x,
        @PositiveOrZero(message = "Crop: y-value has to be positive") int y,
        @Positive(message = "Crop: width has to be positive") int width,
        @Positive(message = "Crop: height has to be positive") int height
) implements TransformationRequest {}
