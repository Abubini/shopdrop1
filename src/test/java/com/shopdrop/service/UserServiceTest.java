package com.shopdrop.service;

import com.shopdrop.model.Role;
import com.shopdrop.model.User;
import com.shopdrop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/** Tests UserService's registration validation in isolation with mocked collaborators. */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
        lenient().when(passwordEncoder.encode(any())).thenReturn("ENCODED");
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void register_withValidDetails_createsAUserWithRoleUser() {
        when(userRepository.existsByEmail("jane@shopdrop.com")).thenReturn(false);

        User user = userService.register("Jane Doe", "jane@shopdrop.com", "password123");

        assertEquals("Jane Doe", user.getName());
        assertEquals("jane@shopdrop.com", user.getEmail());
        assertEquals(Role.USER, user.getRole());
        assertEquals("ENCODED", user.getPassword());
        assertFalse(user.isMember());
    }

    @Test
    void register_normalisesEmailToLowercase() {
        when(userRepository.existsByEmail("jane@shopdrop.com")).thenReturn(false);
        User user = userService.register("Jane Doe", "Jane@ShopDrop.com", "password123");
        assertEquals("jane@shopdrop.com", user.getEmail());
    }

    @Test
    void register_withDuplicateEmail_throwsException() {
        when(userRepository.existsByEmail("jane@shopdrop.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> userService.register("Jane Doe", "jane@shopdrop.com", "password123"));
    }

    @Test
    void register_withBlankName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.register("   ", "jane@shopdrop.com", "password123"));
    }

    @ParameterizedTest(name = "invalid email: {0}")
    @ValueSource(strings = {"not-an-email", "missing-domain@", "@missing-local.com", "no-at-sign.com"})
    void register_withInvalidEmail_throwsException(String badEmail) {
        assertThrows(IllegalArgumentException.class,
                () -> userService.register("Jane Doe", badEmail, "password123"));
    }

    @Test
    void register_withShortPassword_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.register("Jane Doe", "jane@shopdrop.com", "abc12"));
    }

    @Test
    void register_withPasswordAtMinimumLength_succeeds() {
        when(userRepository.existsByEmail("jane@shopdrop.com")).thenReturn(false);
        User user = userService.register("Jane Doe", "jane@shopdrop.com", "abcdef");
        assertEquals("ENCODED", user.getPassword());
    }

    @Test
    void setMembership_updatesTheGivenUser() {
        User user = new User();
        user.setMember(false);

        userService.setMembership(user, true);

        assertTrue(user.isMember());
    }
}
