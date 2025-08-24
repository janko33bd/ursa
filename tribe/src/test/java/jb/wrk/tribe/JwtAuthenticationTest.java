package jb.wrk.tribe;

import jakarta.transaction.Transactional;
import jb.wrk.tribe.demo.components.JwtUtils;
import jb.wrk.tribe.demo.entities.Role;
import jb.wrk.tribe.demo.entities.User;
import jb.wrk.tribe.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JwtAuthenticationTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("demo123"));
        testUser.setRole(Role.USER);

        userRepository.save(testUser);
    }

    @Test
    void shouldGenerateAndValidateJwtToken() {
        // Given
        String token = jwtUtils.generateToken(testUser);

        // When
        String extractedUsername = jwtUtils.extractUsername(token);
        Boolean isValid = jwtUtils.validateToken(token, testUser);

        // Then
        assertThat(extractedUsername).isEqualTo(testUser.getUsername());
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtUtils.generateToken(testUser);

        // When
        String username = jwtUtils.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }
}

