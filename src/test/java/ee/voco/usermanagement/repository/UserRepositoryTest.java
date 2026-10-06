package ee.voco.usermanagement.repository;

import ee.voco.usermanagement.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// JPA andmekihi testid kasutavad H2-d ja iga testi muudatused pööratakse tagasi.
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void saveCreatesUserWithGeneratedId() {
        User user = userRepository.saveAndFlush(new User("Mari", "Tamm", "mari@example.com"));
        entityManager.clear();

        assertThat(user.getId()).isPositive();
        User storedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(storedUser.getFirstName()).isEqualTo("Mari");
        assertThat(storedUser.getLastName()).isEqualTo("Tamm");
        assertThat(storedUser.getEmail()).isEqualTo("mari@example.com");
    }

    @Test
    void findAllReturnsSavedUsers() {
        userRepository.save(new User("Mari", "Tamm", "mari@example.com"));
        userRepository.save(new User("Jaan", "Kask", "jaan@example.com"));
        userRepository.flush();
        entityManager.clear();

        assertThat(userRepository.findAll())
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("mari@example.com", "jaan@example.com");
    }

    @Test
    void findByIdReturnsRequestedUser() {
        User user = userRepository.saveAndFlush(new User("Jaan", "Kask", "jaan@example.com"));
        entityManager.clear();

        User storedUser = userRepository.findById(user.getId()).orElseThrow();

        assertThat(storedUser.getId()).isEqualTo(user.getId());
        assertThat(storedUser.getFirstName()).isEqualTo("Jaan");
        assertThat(storedUser.getLastName()).isEqualTo("Kask");
        assertThat(storedUser.getEmail()).isEqualTo("jaan@example.com");
    }

    @Test
    void saveUpdatesExistingUserWithoutCreatingAnotherRow() {
        User user = userRepository.saveAndFlush(new User("Mari", "Tamm", "mari@example.com"));
        Long id = user.getId();
        entityManager.clear();

        User changedUser = userRepository.findById(id).orElseThrow();
        changedUser.setEmail("uus@example.com");
        userRepository.saveAndFlush(changedUser);
        entityManager.clear();

        User storedUser = userRepository.findById(id).orElseThrow();
        assertThat(storedUser.getEmail()).isEqualTo("uus@example.com");
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void deleteByIdRemovesUserFromDatabase() {
        User user = userRepository.saveAndFlush(new User("Mari", "Tamm", "mari@example.com"));
        Long id = user.getId();
        entityManager.clear();

        userRepository.deleteById(id);
        userRepository.flush();
        entityManager.clear();

        assertThat(userRepository.findById(id)).isEmpty();
        assertThat(userRepository.existsById(id)).isFalse();
    }
}
