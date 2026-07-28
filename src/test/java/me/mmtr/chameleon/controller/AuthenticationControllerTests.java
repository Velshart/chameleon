package me.mmtr.chameleon.controller;

import me.mmtr.chameleon.handler.GlobalExceptionHandler;
import me.mmtr.chameleon.model.dto.AuthenticationRequest;
import me.mmtr.chameleon.model.dto.AuthenticationResponse;
import me.mmtr.chameleon.model.dto.RegisterRequest;
import me.mmtr.chameleon.service.AuthenticationService;
import me.mmtr.chameleon.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTests {

    private static final String USERNAME = "test_user";
    private static final String PASSWORD = "password";
    private static final String TOKEN = "generated.jwt.token";

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn201WithTokenAfterSuccessfulRegistration() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                USERNAME,
                PASSWORD
        );

        when(authenticationService.register(registerRequest))
                .thenReturn(new AuthenticationResponse(TOKEN));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(TOKEN));
    }

    @Test
    void shouldReturn400WhenRegisterRequestHasBlankUsername() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, org.mockito.Mockito.never()).register(any());
    }


    @Test
    void shouldReturn400WhenRegisterRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest(USERNAME, PASSWORD);

        when(authenticationService.register(request))
                .thenThrow(new IllegalArgumentException("Username already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn200WithTokenWhenLoginSucceeds() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest(USERNAME, PASSWORD);

        when(authenticationService.login(request))
                .thenReturn(new AuthenticationResponse(TOKEN));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN));
    }

    @Test
    void shouldReturn400WhenLoginRequestHasBlankPassword() throws Exception {
        AuthenticationRequest invalidRequest =
                new AuthenticationRequest(
                        USERNAME,
                        ""
                );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any());
    }
}
