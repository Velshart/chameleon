package me.mmtr.chameleon.model.dto.request.transformation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record CompressRequest(
        @DecimalMin(
                value = "0.0",
                message = "Compress: quality must be between 0.0 and 1.0"
        )
        @DecimalMax(
                value = "1.0",
                message = "Compress: quality must be between 0.0 and 1.0"
        )
        float quality
) implements TransformationRequest {}