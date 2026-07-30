package me.mmtr.chameleon.controller;

import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.assembler.ImageModelAssembler;
import me.mmtr.chameleon.model.dto.ImageResponse;
import me.mmtr.chameleon.model.images.Image;
import me.mmtr.chameleon.service.ImageService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
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
    private final ImageModelAssembler imageModelAssembler;

    public ImageController(
            ImageService imageService,
            ImageModelAssembler imageModelAssembler
    ) {
        this.imageService = imageService;
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
        byte[] data = imageService.getImageData(id, currentUser);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                image.getOriginalFilename() +
                                "\""
                )
                .body(data);
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
