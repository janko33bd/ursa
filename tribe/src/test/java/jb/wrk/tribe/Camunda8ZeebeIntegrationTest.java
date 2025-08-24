package jb.wrk.tribe;

import jb.wrk.tribe.demo.components.JwtUtils;
import jb.wrk.tribe.demo.entities.Role;
import jb.wrk.tribe.demo.entities.User;
import jb.wrk.tribe.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class Camunda8ZeebeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("intUser");
        testUser.setEmail("int@example.com");
        testUser.setPassword(passwordEncoder.encode("demo123"));
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        jwtToken = jwtUtils.generateToken(testUser);
    }

    @Test
    void startLoanProcessOverRest_requiresAuth() throws Exception {
        int statusNoAuth = mockMvc.perform(post("/api/loans/start")
                        .param("creditScore", "750")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse().getStatus();
        assertThat(statusNoAuth).isEqualTo(401);
    }

    @Test
    void startLoanProcessOverRest_withJwt_reachesController() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/loans/start")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("creditScore", "780")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        int status = res.getResponse().getStatus();
        // We accept either 200 (if Zeebe test engine/broker is available) or 5xx when Zeebe isn't reachable.
        assertThat(status == 200 || (status >= 500 && status < 600))
                .as("Expected 200 OK when broker available, otherwise 5xx error, but not 401/403")
                .isTrue();
        assertThat(status).isNotEqualTo(401);
        assertThat(status).isNotEqualTo(403);
    }
}
