package me.mmtr.chameleon.model.assembler;

import me.mmtr.chameleon.controller.ImageController;
import me.mmtr.chameleon.model.dto.request.ImageResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ImageModelAssembler implements RepresentationModelAssembler<ImageResponse, EntityModel<ImageResponse>> {
    @Override
    public EntityModel<ImageResponse> toModel(ImageResponse entity) {
        return EntityModel.of(
                entity,
                linkTo(methodOn(ImageController.class)
                        .getImage(entity.id(), null))
                        .withSelfRel(),
                linkTo(methodOn(ImageController.class)
                        .listOwnerImages(
                                null,
                                null,
                                null
                        ))
                        .withRel("images"),
                linkTo(methodOn(ImageController.class)
                        .transformImage(
                                entity.id(),
                                null,
                                null
                        ))
                        .withRel("transform"),
                linkTo(methodOn(ImageController.class)
                        .delete(entity.id(), null))
                        .withRel("delete")
        );
    }

    @Override
    public CollectionModel<EntityModel<ImageResponse>> toCollectionModel(Iterable<? extends ImageResponse> entities) {
        List<EntityModel<ImageResponse>> models = StreamSupport.stream(
                        entities.spliterator(), false)
                .map(this::toModel)
                .toList();

        return CollectionModel.of(
                models,
                linkTo(methodOn(ImageController.class)
                        .listOwnerImages(
                                null,
                                null,
                                null
                        ))
                        .withSelfRel()
        );
    }
}
