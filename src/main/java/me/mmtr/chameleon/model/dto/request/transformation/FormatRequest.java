package me.mmtr.chameleon.model.dto.request.transformation;

import jakarta.validation.constraints.NotBlank;

public record FormatRequest(
        @NotBlank(message = "Format: format cannot be empty") String format
) implements TransformationRequest {}
