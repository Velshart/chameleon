package me.mmtr.chameleon.service.authentication;

import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.model.dto.request.AuthenticationRequest;
import me.mmtr.chameleon.model.dto.response.AuthenticationResponse;
import me.mmtr.chameleon.model.dto.request.RegisterRequest;
import me.mmtr.chameleon.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new IllegalArgumentException("Username already in use");
        }

        User user = new User(
                registerRequest.username(),
                passwordEncoder.encode(registerRequest.password())
        );

        userRepository.save(user);

        return new AuthenticationResponse(jwtService.generateToken(user));
    }

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.username(),
                        authenticationRequest.password()
                )
        );

        User user = userRepository.findByUsername(authenticationRequest.username())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Invalid login credentials provided")
                );

        return new AuthenticationResponse(jwtService.generateToken(user));
    }
}
