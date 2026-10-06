package ee.voco.usermanagement.controller;

import ee.voco.usermanagement.model.User;
import ee.voco.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Kontrollib koos MVC, Thymeleafi, valideerimist ja JPA-d eraldatud H2 andmebaasiga.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void homeRedirectsToUsers() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
    }

    @Test
    void usersPageShowsEmptyMessage() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(content().string(containsString("Kasutajate haldus")))
                .andExpect(content().string(containsString("Kasutajaid ei ole veel lisatud.")))
                .andExpect(content().string(containsString("href=\"/users/new\"")));
    }

    @Test
    void newUserPageShowsForm() throws Exception {
        mockMvc.perform(get("/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-form"))
                .andExpect(content().string(containsString("Lisa kasutaja")))
                .andExpect(content().string(containsString("action=\"/users/save\"")))
                .andExpect(content().string(containsString("name=\"id\"")));
    }

    @Test
    void completeCrudFlowPersistsChangesBetweenRequests() throws Exception {
        mockMvc.perform(post("/users/save")
                        .param("firstName", "Mari")
                        .param("lastName", "Tamm")
                        .param("email", "mari@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        assertEquals(1, userRepository.count());
        User savedUser = userRepository.findAll().get(0);
        Long id = savedUser.getId();
        assertEquals("Mari", savedUser.getFirstName());
        assertEquals("Tamm", savedUser.getLastName());
        assertEquals("mari@example.com", savedUser.getEmail());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("mari@example.com")))
                .andExpect(content().string(containsString("href=\"/users/edit/" + id + "\"")))
                .andExpect(content().string(containsString("href=\"/users/delete/" + id + "\"")));

        mockMvc.perform(get("/users/edit/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("user-form"))
                .andExpect(content().string(containsString("Muuda kasutajat")))
                .andExpect(content().string(containsString("value=\"mari@example.com\"")))
                .andExpect(content().string(containsString("value=\"" + id + "\"")));

        mockMvc.perform(post("/users/save")
                        .param("id", id.toString())
                        .param("firstName", "Mari")
                        .param("lastName", "Tamm")
                        .param("email", "uus@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        assertEquals(1, userRepository.count());
        assertEquals("uus@example.com", userRepository.findById(id).orElseThrow().getEmail());
        mockMvc.perform(get("/users"))
                .andExpect(content().string(containsString("uus@example.com")));

        mockMvc.perform(get("/users/delete/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        assertFalse(userRepository.existsById(id));
        mockMvc.perform(get("/users"))
                .andExpect(content().string(containsString("Kasutajaid ei ole veel lisatud.")));
    }

    @Test
    void invalidFormShowsErrorsAndDoesNotSaveUser() throws Exception {
        mockMvc.perform(post("/users/save")
                        .param("firstName", " ")
                        .param("lastName", "")
                        .param("email", "vigane-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-form"))
                .andExpect(model().attributeHasFieldErrors("user", "firstName", "lastName", "email"))
                .andExpect(content().string(containsString("Eesnimi ei tohi olla tühi.")))
                .andExpect(content().string(containsString("Perekonnanimi ei tohi olla tühi.")))
                .andExpect(content().string(containsString("Sisesta korrektne e-posti aadress.")));

        assertEquals(0, userRepository.count());
    }

    @Test
    void blankEmailIsRejected() throws Exception {
        mockMvc.perform(post("/users/save")
                        .param("firstName", "Mari")
                        .param("lastName", "Tamm")
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("user", "email"))
                .andExpect(content().string(containsString("E-post ei tohi olla tühi.")));

        assertEquals(0, userRepository.count());
    }

    @Test
    void invalidEditPreservesStoredData() throws Exception {
        User user = userRepository.save(new User("Mari", "Tamm", "mari@example.com"));

        mockMvc.perform(post("/users/save")
                        .param("id", user.getId().toString())
                        .param("firstName", "Mari")
                        .param("lastName", "Tamm")
                        .param("email", "vigane-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-form"))
                .andExpect(content().string(containsString("Muuda kasutajat")))
                .andExpect(model().attributeHasFieldErrors("user", "email"));

        assertEquals("mari@example.com", userRepository.findById(user.getId()).orElseThrow().getEmail());
    }

    @Test
    void missingUsersReturnNotFound() throws Exception {
        mockMvc.perform(get("/users/edit/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/users/delete/999999")).andExpect(status().isNotFound());
        mockMvc.perform(post("/users/save")
                        .param("id", "999999")
                        .param("firstName", "Mari")
                        .param("lastName", "Tamm")
                        .param("email", "mari@example.com"))
                .andExpect(status().isNotFound());

        assertTrue(userRepository.findAll().isEmpty());
    }

    @Test
    void overlyLongNameIsRejectedBeforeDatabaseWrite() throws Exception {
        mockMvc.perform(post("/users/save")
                        .param("firstName", "a".repeat(256))
                        .param("lastName", "Tamm")
                        .param("email", "mari@example.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("user", "firstName"));

        assertEquals(0, userRepository.count());
    }
}
