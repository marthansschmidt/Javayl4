package ee.voco.usermanagement.service;

import ee.voco.usermanagement.model.User;
import ee.voco.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void getAllUsersReturnsRepositoryUsers() {
        User user = new User("Mari", "Tamm", "mari@example.com");
        when(userRepository.findAll()).thenReturn(List.of(user));

        assertEquals(List.of(user), userService.getAllUsers());
        verify(userRepository).findAll();
    }

    @Test
    void getUserByIdReturnsExistingUser() {
        User user = new User("Mari", "Tamm", "mari@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertSame(user, userService.getUserById(1L));
    }

    @Test
    void getUserByIdThrowsNotFoundForMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.getUserById(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void saveUserSavesNewUser() {
        User user = new User("Mari", "Tamm", "mari@example.com");
        User savedUser = new User("Mari", "Tamm", "mari@example.com");
        savedUser.setId(1L);
        when(userRepository.save(user)).thenReturn(savedUser);

        assertSame(savedUser, userService.saveUser(user));
        verify(userRepository).save(user);
    }

    @Test
    void saveUserUpdatesExistingUser() {
        User existingUser = new User("Mari", "Tamm", "mari@example.com");
        existingUser.setId(1L);
        User changedUser = new User("Maria", "Kask", "maria@example.com");
        changedUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        User result = userService.saveUser(changedUser);

        assertEquals(1L, result.getId());
        assertEquals("Maria", result.getFirstName());
        assertEquals("Kask", result.getLastName());
        assertEquals("maria@example.com", result.getEmail());
        verify(userRepository).save(existingUser);
    }

    @Test
    void saveUserDoesNotCreateUserWithMissingId() {
        User user = new User("Mari", "Tamm", "mari@example.com");
        user.setId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.saveUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserDeletesExistingUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserThrowsNotFoundForMissingUser() {
        when(userRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.deleteUser(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(userRepository, never()).deleteById(anyLong());
    }
}
