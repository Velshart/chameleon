package me.mmtr.chameleon.model.assembler;

import me.mmtr.chameleon.controller.ImageController;
import me.mmtr.chameleon.model.dto.ImageResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ImageModelAssembler implements RepresentationModelAssembler<@NonNull ImageResponse, @NonNull EntityModel<ImageResponse>> {
    @Override
    public EntityModel<@NonNull ImageResponse> toModel(ImageResponse entity) {
        return EntityModel.of(
                entity,
                linkTo(methodOn(ImageController.class).getImage(entity.id(), null))
                        .withSelfRel(),
                linkTo(methodOn(ImageController.class).listOwnerImages(null))
                        .withRel("images"),
                linkTo(methodOn(ImageController.class).delete(entity.id(), null))
                        .withRel("delete")
        );
    }
}
