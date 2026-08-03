package me.mmtr.chameleon.controller;

import jakarta.validation.Valid;
import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.assembler.ImageModelAssembler;
import me.mmtr.chameleon.model.dto.request.ImageResponse;
import me.mmtr.chameleon.model.dto.request.transformation.TransformImageRequest;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.service.images.ImageService;
import me.mmtr.chameleon.service.images.ImageTransformationService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;
    private final ImageTransformationService imageTransformationService;
    private final ImageModelAssembler imageModelAssembler;

    public ImageController(
            ImageService imageService,
            ImageTransformationService imageTransformationService,
            ImageModelAssembler imageModelAssembler
    ) {
        this.imageService = imageService;
        this.imageTransformationService = imageTransformationService;
        this.imageModelAssembler = imageModelAssembler;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntityModel<ImageResponse>> upload(
            @RequestParam(value = "file") MultipartFile file,
            @AuthenticationPrincipal User currentUser
    ) {
        ImageResponse imageResponse = imageService.upload(file, currentUser);
        EntityModel<ImageResponse> entityModel =
                imageModelAssembler.toModel(imageResponse);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(entityModel);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ImageResponse>>> listOwnerImages(
            @AuthenticationPrincipal User currentUser
    ) {
        List<EntityModel<ImageResponse>> imagesEntityModelList =
                imageService.getAllForOwner(currentUser)
                        .stream()
                        .map(imageModelAssembler::toModel)
                        .toList();

        CollectionModel<EntityModel<ImageResponse>> collectionModel =
                CollectionModel.of(
                        imagesEntityModelList,
                        linkTo(methodOn(ImageController.class)
                                .listOwnerImages(null)
                        ).withSelfRel()
                );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        Image image = imageService.getImageForOwner(id, currentUser);
        byte[] data = imageService.getImageDataAsBytes(id, currentUser);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(image.getContentType())
                ).body(data);
    }

    @PostMapping("/{id}/transform")
    public ResponseEntity<byte[]> transformImage(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TransformImageRequest request
    ) {
        ImageTransformationService.TransformedImage transformedImage =
                imageTransformationService.transformImage(
                        id,
                        currentUser,
                        request.transformations()
                );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "image/" + transformedImage.format()
                )).body(transformedImage.imageData());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        imageService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
