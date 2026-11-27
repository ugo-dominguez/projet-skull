package fr.miage.skull.playerservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlayerAuthTests {

    @Autowired
    MockMvc mvc;

    @Test
    void createThenLogin() throws Exception {
        String email = "junit-" + System.currentTimeMillis() + "@skull.fr";

        mvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"username\":\"JUnit\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.admin").value(false));

        mvc.perform(post("/players/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        mvc.perform(post("/players/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@skull.fr\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailRejected() throws Exception {
        String email = "dup-" + System.currentTimeMillis() + "@skull.fr";
        String body = "{\"email\":\"" + email + "\",\"username\":\"Dup\",\"password\":\"secret123\"}";

        mvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }
}
