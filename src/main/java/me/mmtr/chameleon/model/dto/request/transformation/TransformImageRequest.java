package me.mmtr.chameleon.model.dto.request.transformation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TransformImageRequest(
        @NotEmpty(message = "Transform: List of transformations cannot be empty")
        List<@Valid TransformationRequest> transformations
) {}
