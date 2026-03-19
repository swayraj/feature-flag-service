package com.flagservice.feature_flag_service.controller;

import tools.jackson.databind.ObjectMapper;
import com.flagservice.feature_flag_service.model.Flag;
import com.flagservice.feature_flag_service.repository.FlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Testcontainers
class FlagControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlagRepository flagRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        flagRepository.deleteAll();
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────

    @Test
    void getAllFlags_emptyDb_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/flags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Test
    void createFlag_validFlag_returns201WithCreatedFlag() throws Exception {
        Flag flag = new Flag(null, "test_flag", "Integration test flag", true, 50);

        mockMvc.perform(post("/api/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flag)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("test_flag"))
                .andExpect(jsonPath("$.rolloutPercentage").value(50));
    }

    @Test
    void createFlag_duplicateName_returns400() throws Exception {
        Flag flag = new Flag(null, "test_flag", "desc", true, 50);
        flagRepository.save(flag);

        mockMvc.perform(post("/api/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flag)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("already exists")));
    }

    @Test
    void createFlag_nameTooShort_returns400() throws Exception {
        Flag flag = new Flag(null, "ab", "too short", true, 50);

        mockMvc.perform(post("/api/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flag)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFlag_rolloutOver100_returns400() throws Exception {
        Flag flag = new Flag(null, "valid_flag", "desc", true, 150);

        mockMvc.perform(post("/api/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flag)))
                .andExpect(status().isBadRequest());
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────

    @Test
    void getFlagById_existingFlag_returns200() throws Exception {
        Flag saved = flagRepository.save(new Flag(null, "test_flag", "desc", true, 50));

        mockMvc.perform(get("/api/flags/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test_flag"));
    }

    @Test
    void getFlagById_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/flags/99999"))
                .andExpect(status().isNotFound());
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    void updateFlag_validUpdate_returns200WithUpdatedData() throws Exception {
        Flag saved = flagRepository.save(new Flag(null, "test_flag", "old desc", false, 0));
        Flag update = new Flag(null, "test_flag", "new desc", true, 75);

        mockMvc.perform(put("/api/flags/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("new desc"))
                .andExpect(jsonPath("$.rolloutPercentage").value(75))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    // ── TOGGLE ───────────────────────────────────────────────────────────────

    @Test
    void toggleFlag_enabledFlag_disablesIt() throws Exception {
        Flag saved = flagRepository.save(new Flag(null, "test_flag", "desc", true, 50));

        mockMvc.perform(post("/api/flags/" + saved.getId() + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void toggleFlag_disabledFlag_enablesIt() throws Exception {
        Flag saved = flagRepository.save(new Flag(null, "test_flag", "desc", false, 50));

        mockMvc.perform(post("/api/flags/" + saved.getId() + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Test
    void deleteFlag_existingFlag_returns204ThenGone() throws Exception {
        Flag saved = flagRepository.save(new Flag(null, "test_flag", "desc", true, 50));

        mockMvc.perform(delete("/api/flags/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/flags/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    // ── SEARCH & FILTER ───────────────────────────────────────────────────────

    @Test
    void getEnabledFlags_onlyReturnsEnabledFlags() throws Exception {
        flagRepository.save(new Flag(null, "enabled_flag", "d", true, 50));
        flagRepository.save(new Flag(null, "disabled_flag", "d", false, 0));

        mockMvc.perform(get("/api/flags/enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("enabled_flag"));
    }

    @Test
    void searchFlags_byPartialName_returnsMatches() throws Exception {
        flagRepository.save(new Flag(null, "dark_mode", "d", true, 50));
        flagRepository.save(new Flag(null, "dark_theme", "d", true, 50));
        flagRepository.save(new Flag(null, "new_checkout", "d", true, 50));

        mockMvc.perform(get("/api/flags/search?name=dark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
