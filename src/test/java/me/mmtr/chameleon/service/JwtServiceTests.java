package me.mmtr.chameleon.service;

import io.jsonwebtoken.JwtException;
import me.mmtr.chameleon.service.authentication.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTests {
    private static final String TEST_SECRET =
            "NDA0RTYzNUY2QTU4NkU1QzY2MzM4RDQ5MzQ1NDMxMzMzMzc4MzMzMzM=";
    private static final long ONE_HOUR_IN_MILLIS = 3600000;

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(
                jwtService,
                "jwtExpiration",
                ONE_HOUR_IN_MILLIS
        );

        userDetails = new User(
                "test_user",
                "password",
                Collections.emptyList()
        );
    }

    @Test
    void shouldGenerateNonEmptyToken() {
        String generatedToken = jwtService.generateToken(userDetails);

        assertThat(generatedToken).isNotBlank();
        assertEquals(3, generatedToken.split("\\.").length);
    }

    @Test
    void shouldCorrectlyExtractUsernameFromToken() {
        String generatedToken = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(generatedToken);

        assertEquals("test_user", username);
    }

    @Test
    void shouldValidateTokenForCorrectUser() {
        String generatedToken = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(generatedToken, userDetails);

        assertTrue(isValid);
    }

    @Test
    void shouldRejectTokenForInvalidUser() {
        String generatedToken = jwtService.generateToken(userDetails);
        UserDetails otherUser = new User(
                "other_user",
                "password",
                Collections.emptyList()
        );

        boolean isValid = jwtService.isTokenValid(generatedToken, otherUser);

        assertFalse(isValid);
    }

    @Test
    void shouldRejectExpiredToken() {
        ReflectionTestUtils.setField(
                jwtService,
                "jwtExpiration",
                -ONE_HOUR_IN_MILLIS
        );
        String generatedToken = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(generatedToken, userDetails);

        assertFalse(isValid);
    }

    @Test
    void shouldThrowExceptionForMalformedToken() {
        assertThrows(JwtException.class, () ->
                jwtService.extractUsername("not.a.valid.token")
        );
    }

    @Test
    void shouldThrowExceptionForTokenSignedWithDifferentSecret() {
        String differentSecret = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";
        JwtService otherJwtService = new JwtService();
        ReflectionTestUtils.setField(
                otherJwtService,
                "jwtSecret",
                differentSecret
        );
        ReflectionTestUtils.setField(
                otherJwtService,
                "jwtExpiration",
                ONE_HOUR_IN_MILLIS
        );

        String generatedToken = otherJwtService.generateToken(userDetails);

        assertThrows(JwtException.class,
                () -> jwtService.extractUsername(generatedToken));
    }
}
