package me.mmtr.chameleon.model.dto.request.transformation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.coobird.thumbnailator.geometry.Positions;

public record WatermarkRequest(
        @NotBlank(message = "Watermark: text cannot be empty") String text,
        @DecimalMin(
                value = "0.0",
                message = "Watermark: opacity must be between 0.0 and 1.0"
        )
        @DecimalMax(
                value = "1.0",
                message = "Watermark: opacity must be between 0.0 and 1.0"
        )
        float opacity,
        @NotNull(message = "Watermark: position cannot be null")
        Positions position
) implements TransformationRequest {}
