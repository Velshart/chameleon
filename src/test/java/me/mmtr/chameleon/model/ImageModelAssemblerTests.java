package me.mmtr.chameleon.model;

import me.mmtr.chameleon.model.assembler.ImageModelAssembler;
import me.mmtr.chameleon.model.dto.request.ImageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ImageModelAssemblerTests {

    private final ImageModelAssembler assembler = new ImageModelAssembler();

    @BeforeEach
    void beforeEach() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder
                .setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void shouldBuildEntityModelWithCorrectLinks() {
        ImageResponse response = new ImageResponse(
                1L,
                "cat.jpg",
                "image/jpeg",
                12345L,
                Instant.parse("2026-07-29T10:00:00Z")
        );

        EntityModel<ImageResponse> model = assembler.toModel(response);

        assertThat(model.getContent()).isEqualTo(response);

        assertThat(model.getLink("self"))
                .isPresent()
                .map(Link::getHref)
                .hasValueSatisfying(href ->
                        assertThat(href).endsWith("/api/images/1"));

        assertThat(model.getLink("images"))
                .isPresent()
                .map(Link::getHref)
                .hasValueSatisfying(href ->
                        assertThat(href).endsWith("/api/images"));

        assertThat(model.getLink("transform"))
                .isPresent()
                .map(Link::getHref)
                .hasValueSatisfying(href ->
                        assertThat(href).endsWith("/api/images/1/transform"));

        assertThat(model.getLink("delete"))
                .isPresent()
                .map(Link::getHref)
                .hasValueSatisfying(href ->
                        assertThat(href).endsWith("/api/images/1"));
    }

    @Test
    void shouldBuildCollectionModelWithCorrectSelfLink() {
        ImageResponse responseA = new ImageResponse(
                1L,
                "cat.jpg",
                "image/jpeg",
                12345L,
                Instant.parse("2026-07-29T10:00:00Z")
        );

        ImageResponse responseB = new ImageResponse(
                2L,
                "dog.png",
                "image/png",
                2048L,
                Instant.parse("2026-07-29T10:00:00Z")
        );

        var collectionModel = assembler
                .toCollectionModel(List.of(responseA, responseB));

        assertThat(collectionModel.getContent().size()).isEqualTo(2);

        assertThat(collectionModel.getLink("self"))
                .isPresent()
                .map(Link::getHref)
                .hasValueSatisfying(href ->
                        assertThat(href).endsWith("/api/images")
                );

    }
}
