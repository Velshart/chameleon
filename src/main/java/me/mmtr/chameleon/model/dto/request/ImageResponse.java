package me.mmtr.chameleon.model.dto.request;

import me.mmtr.chameleon.model.images.Image;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Relation(itemRelation = "image", collectionRelation = "images")
public record ImageResponse(
        Long id,
        String originalFilename,
        String contentType,
        long size,
        Instant uploadedAt
) {
    public static ImageResponse fromImage(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getOriginalFilename(),
                image.getContentType(),
                image.getSize(),
                image.getUploadedAt()
        );
    }
}
