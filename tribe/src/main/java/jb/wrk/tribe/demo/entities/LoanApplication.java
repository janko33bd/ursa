package jb.wrk.tribe.demo.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String applicantName;

    @Column(nullable = false)
    private String applicantEmail;

    @Column(nullable = false)
    private BigDecimal requestedAmount;

    @Column(nullable = false)
    private Integer creditScore;

    @Column(nullable = false)
    private BigDecimal annualIncome;

    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.SUBMITTED;

    private String processInstanceId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
