package me.mmtr.chameleon.service;

import me.mmtr.chameleon.model.User;
import me.mmtr.chameleon.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTests {

    private final String USERNAME = "test_user";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldLoadUserWhenCorrectUsernameProvided() {
        User user = new User(
                USERNAME,
                "password"
        );

        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(user));

        UserDetails loadedUser = userDetailsService.loadUserByUsername(USERNAME);

        assertEquals(user, loadedUser);
    }

    @Test
    void shouldThrowExceptionWhenUsernameNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(USERNAME)
        );
    }
}
