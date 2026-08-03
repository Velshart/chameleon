package me.mmtr.chameleon.exception;

import me.mmtr.chameleon.model.dto.request.transformation.TransformationRequest;

public class UnsupportedTransformationException extends RuntimeException {
    public UnsupportedTransformationException(
            Class<? extends TransformationRequest> aClass
    ) {
        super("Unsupported transformation type: " + aClass.getSimpleName());
    }
}
