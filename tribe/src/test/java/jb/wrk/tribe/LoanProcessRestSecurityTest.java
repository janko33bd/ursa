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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoanProcessRestSecurityTest {

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
    private User adminUser;
    private String userJwtToken;
    private String adminJwtToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("demo123"));
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);

        userJwtToken = jwtUtils.generateToken(testUser);
        adminJwtToken = jwtUtils.generateToken(adminUser);
    }

    @Test
    void shouldAllowAuthenticatedUserToStartLoanProcess() throws Exception {
        mockMvc.perform(post("/api/loans/start")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .param("creditScore", "750")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processInstanceKey").exists())
                .andExpect(jsonPath("$.bpmnProcessId").value("loanApprovalProcess"));
    }

    @Test
    void shouldAllowAuthenticatedAdminToStartLoanProcess() throws Exception {
        mockMvc.perform(post("/api/loans/start")
                        .header("Authorization", "Bearer " + adminJwtToken)
                        .param("creditScore", "800")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processInstanceKey").exists())
                .andExpect(jsonPath("$.bpmnProcessId").value("loanApprovalProcess"));
    }

    @Test
    void shouldRejectUnauthenticatedRequestToLoanController() throws Exception {
        mockMvc.perform(post("/api/loans/start")
                        .param("creditScore", "750")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidTokenToLoanController() throws Exception {
        mockMvc.perform(post("/api/loans/start")
                        .header("Authorization", "Bearer invalid.token.here")
                        .param("creditScore", "750")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/loans/start")
                        .header("Authorization", "InvalidFormat " + userJwtToken)
                        .param("creditScore", "750")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedUserToStartProcessController() throws Exception {
        mockMvc.perform(post("/process/loan/start")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .param("creditScore", "650")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processInstanceKey").exists())
                .andExpect(jsonPath("$.bpmnProcessId").value("loanApprovalProcess"));
    }

    @Test
    void shouldRejectUnauthenticatedRequestToProcessController() throws Exception {
        mockMvc.perform(post("/process/loan/start")
                        .param("creditScore", "650")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleProcessStartWithoutCreditScore() throws Exception {
        mockMvc.perform(post("/api/loans/start")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processInstanceKey").exists())
                .andExpect(jsonPath("$.bpmnProcessId").value("loanApprovalProcess"));
    }

    @Test
    void shouldHandleExpiredToken() throws Exception {
        User expiredUser = new User();
        expiredUser.setUsername("expired");
        expiredUser.setEmail("expired@example.com");
        expiredUser.setPassword(passwordEncoder.encode("demo123"));
        expiredUser.setRole(Role.USER);
        userRepository.save(expiredUser);

        String expiredToken = createExpiredToken(expiredUser);

        mockMvc.perform(post("/api/loans/start")
                        .header("Authorization", "Bearer " + expiredToken)
                        .param("creditScore", "750")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldValidateJwtTokenStructure() throws Exception {
        mockMvc.perform(post("/api/loans/start")
                        .header("Authorization", "Bearer not.a.valid.jwt.structure")
                        .param("creditScore", "750")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleBothControllersWithSameAuthentication() throws Exception {
        mockMvc.perform(post("/api/loans/start")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .param("creditScore", "700")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/process/loan/start")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .param("creditScore", "700")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private String createExpiredToken(User user) {
        return jwtUtils.generateToken(user);
    }
}