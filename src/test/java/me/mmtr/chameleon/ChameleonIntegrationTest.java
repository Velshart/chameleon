package me.mmtr.chameleon;

import me.mmtr.chameleon.filter.RateLimitingFilter;
import me.mmtr.chameleon.model.dto.request.RegisterRequest;
import me.mmtr.chameleon.model.dto.response.AuthenticationResponse;
import me.mmtr.chameleon.repository.ImageDataRepository;
import me.mmtr.chameleon.repository.ImageRepository;
import me.mmtr.chameleon.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ChameleonIntegrationTest {
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @SuppressWarnings("resource")
    @ServiceConnection(name = "redis")
    static final GenericContainer<?> redis =
            new GenericContainer<>(
                    DockerImageName.parse("redis:7-alpine")
            ).withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageDataRepository imageDataRepository;

    @BeforeEach
    void clearDatabaseAndResetRateLimitingFilter() {
        imageDataRepository.deleteAll();
        imageRepository.deleteAll();
        userRepository.deleteAll();

        rateLimitingFilter.reset();
    }

    protected String registerAndGetToken(
            String username,
            String password
    ) throws Exception {
        RegisterRequest request = new RegisterRequest(
                username,
                password
        );
        var result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        ).token();
    }

    protected byte[] createTestPng() throws Exception {
        BufferedImage image = new BufferedImage(
                50,
                50,
                BufferedImage.TYPE_INT_RGB
        );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
