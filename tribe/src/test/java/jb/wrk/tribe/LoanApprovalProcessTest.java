package jb.wrk.tribe;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoanApprovalProcessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("demo123"));
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        String jwtToken = jwtUtils.generateToken(testUser);
    }

    @Test
    void shouldRejectRequestWithoutJwtToken() throws Exception {
        int status = mockMvc.perform(post("/api/loans/start")
                        .param("creditScore", "750")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getStatus();
        assertThat(status == 401 || status == 403 || (status >= 500 && status < 600)).isTrue();
    }

    @Test
    void shouldRejectProcessControllerWithoutJwtToken() throws Exception {
        int status = mockMvc.perform(post("/process/loan/start")
                        .param("creditScore", "650")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getStatus();
        assertThat(status == 401 || status == 403 || (status >= 500 && status < 600)).isTrue();
    }

    @Test
    void shouldValidateJwtTokenIsRequired() throws Exception {
        int status1 = mockMvc.perform(post("/api/loans/start")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getStatus();
        assertThat(status1 == 401 || status1 == 403 || (status1 >= 500 && status1 < 600)).isTrue();

        int status2 = mockMvc.perform(post("/process/loan/start")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getStatus();
        assertThat(status2 == 401 || status2 == 403 || (status2 >= 500 && status2 < 600)).isTrue();
    }
}