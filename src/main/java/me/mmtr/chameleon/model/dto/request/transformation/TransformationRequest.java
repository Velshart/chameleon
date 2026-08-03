package me.mmtr.chameleon.model.dto.request.transformation;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ResizeRequest.class, name = "RESIZE"),
        @JsonSubTypes.Type(value = CropRequest.class, name = "CROP"),
        @JsonSubTypes.Type(value = RotateRequest.class, name = "ROTATE"),
        @JsonSubTypes.Type(value = WatermarkRequest.class, name = "WATERMARK"),
        @JsonSubTypes.Type(value = FlipRequest.class, name = "FLIP"),
        @JsonSubTypes.Type(value = MirrorRequest.class, name = "MIRROR"),
        @JsonSubTypes.Type(value = CompressRequest.class, name = "COMPRESS"),
        @JsonSubTypes.Type(value = FormatRequest.class, name = "FORMAT"),
        @JsonSubTypes.Type(value = GrayscaleRequest.class, name = "GRAYSCALE"),
        @JsonSubTypes.Type(value = SepiaRequest.class, name = "SEPIA")
})
public sealed interface TransformationRequest permits
        ResizeRequest,
        CropRequest,
        RotateRequest,
        WatermarkRequest,
        FlipRequest,
        MirrorRequest,
        CompressRequest,
        FormatRequest,
        GrayscaleRequest,
        SepiaRequest {
}
