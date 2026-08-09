package me.mmtr.chameleon.service.authentication;

import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.dto.request.AuthenticationRequest;
import me.mmtr.chameleon.model.dto.response.AuthenticationResponse;
import me.mmtr.chameleon.model.dto.request.RegisterRequest;
import me.mmtr.chameleon.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {

    private static final String USERNAME = "testuser";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String TOKEN = "generated.jwt.token";
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldRegisterNewUserAndReturnToken() {
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(jwtService.generateToken(any(User.class))).thenReturn(TOKEN);

        RegisterRequest registerRequest = new RegisterRequest(
                USERNAME,
                RAW_PASSWORD
        );

        AuthenticationResponse response =
                authenticationService.register(registerRequest);

        assertEquals(TOKEN, response.token());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(USERNAME, savedUser.getUsername());
        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyRegistered() {
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        RegisterRequest registerRequest = new RegisterRequest(
                USERNAME,
                RAW_PASSWORD
        );

        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.register(registerRequest)
        );

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void shouldLoginWithValidCredentialsAndReturnToken() {
        User user = new User(USERNAME, ENCODED_PASSWORD);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(TOKEN);

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(
                        USERNAME,
                        RAW_PASSWORD
                );

        AuthenticationResponse authenticationResponse = authenticationService.login(
                authenticationRequest
        );

        assertEquals(TOKEN, authenticationResponse.token());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authenticationCaptor.capture());


        UsernamePasswordAuthenticationToken capturedToken =
                authenticationCaptor.getValue();

        assertEquals(USERNAME, capturedToken.getPrincipal());
        assertEquals(RAW_PASSWORD, capturedToken.getCredentials());
    }

    @Test
    void shouldThrowExceptionWhenInvalidCredentialsProvided() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(
                        USERNAME,
                        RAW_PASSWORD
                );

        assertThrows(BadCredentialsException.class,
                () -> authenticationService.login(authenticationRequest)
        );

        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtService, never()).generateToken(any());

    }

    @Test
    void shouldThrowExceptionWhenUsernameNotFound() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(
                        USERNAME,
                        RAW_PASSWORD
                );

        assertThrows(UsernameNotFoundException.class,
                () -> authenticationService.login(authenticationRequest)
        );

        verify(jwtService, never()).generateToken(any());
    }
}
