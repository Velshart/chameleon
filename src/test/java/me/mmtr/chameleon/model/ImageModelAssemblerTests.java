package me.mmtr.chameleon.model;

import me.mmtr.chameleon.model.assembler.ImageModelAssembler;
import me.mmtr.chameleon.model.dto.ImageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ImageModelAssemblerTests {

    private final ImageModelAssembler assembler = new ImageModelAssembler();

    @Test
    void shouldBuildEntityModelWithCorrectLinks() {
        withMockRequestContext(() -> {
            ImageResponse response = new ImageResponse(
                    1L,
                    "cat.jpg",
                    "image/jpeg",
                    12345L,
                    Instant.parse("2026-07-29T10:00:00Z")
            );

            EntityModel<ImageResponse> model = assembler.toModel(response);

            assertThat(model.getLink("self"))
                    .isPresent()
                    .get()
                    .extracting(Link::getHref)
                    .isEqualTo("http://localhost/api/images/1");

            assertThat(model.getLink("delete"))
                    .isPresent()
                    .get()
                    .extracting(Link::getHref)
                    .isEqualTo("http://localhost/api/images/1");

            assertThat(model.getLink("images"))
                    .isPresent()
                    .get()
                    .extracting(Link::getHref)
                    .isEqualTo("http://localhost/api/images");
        });
    }

    private void withMockRequestContext(Runnable test) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            test.run();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
