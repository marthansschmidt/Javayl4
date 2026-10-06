package ee.voco.usermanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Eesnimi ei tohi olla tühi.")
    @Size(max = 255, message = "Eesnimi võib olla kuni 255 märki pikk.")
    private String firstName;

    @NotBlank(message = "Perekonnanimi ei tohi olla tühi.")
    @Size(max = 255, message = "Perekonnanimi võib olla kuni 255 märki pikk.")
    private String lastName;

    @NotBlank(message = "E-post ei tohi olla tühi.")
    @Email(message = "Sisesta korrektne e-posti aadress.")
    @Size(max = 255, message = "E-post võib olla kuni 255 märki pikk.")
    private String email;

    public User() {
    }

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
