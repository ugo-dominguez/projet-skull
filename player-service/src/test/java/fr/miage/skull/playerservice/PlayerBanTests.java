package fr.miage.skull.playerservice;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlayerBanTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    private JsonNode createPlayer(String email, String username) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"username\":\"" + username + "\",\"password\":\"secret123\"}";
        String resp = mvc.perform(post("/players").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resp);
    }

    @Test
    void adminCanBanAndUnban() throws Exception {
        String email = "ban-" + System.currentTimeMillis() + "@skull.fr";
        JsonNode victim = createPlayer(email, "Victime");

        // Récupérer l'ID de l'admin (créé au démarrage)
        String adminResp = mvc.perform(post("/players/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@skull.fr\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String adminId = json.readTree(adminResp).get("id").asText();

        // Ban
        mvc.perform(put("/players/" + victim.get("id").asText() + "/ban")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.banned").value(true));

        // Unban
        mvc.perform(put("/players/" + victim.get("id").asText() + "/unban")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.banned").value(false));
    }

    @Test
    void nonAdminCannotBan() throws Exception {
        String email = "notadmin-" + System.currentTimeMillis() + "@skull.fr";
        JsonNode caller = createPlayer(email, "PasAdmin");

        String victimEmail = "victim2-" + System.currentTimeMillis() + "@skull.fr";
        JsonNode victim = createPlayer(victimEmail, "Victime2");

        mvc.perform(put("/players/" + victim.get("id").asText() + "/ban")
                        .header("X-User-Id", caller.get("id").asText()))
                .andExpect(status().isForbidden());
    }

    @Test
    void reportFlow() throws Exception {
        String r1 = "reporter-" + System.currentTimeMillis() + "@skull.fr";
        String r2 = "reported-" + System.currentTimeMillis() + "@skull.fr";
        JsonNode reporter = createPlayer(r1, "Signaleur");
        JsonNode reported = createPlayer(r2, "Tricheur");

        String body = "{\"reporterId\":\"" + reporter.get("id").asText() + "\","
                + "\"reportedId\":\"" + reported.get("id").asText() + "\","
                + "\"gameId\":null,\"reason\":\"Triche\"}";

        mvc.perform(post("/reports").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reason").value("Triche"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Triche"));
    }
}
