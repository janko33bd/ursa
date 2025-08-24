package jb.wrk.tribe.demo.components;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
@org.springframework.context.annotation.Profile("!test")
public class ZeebeWorkers {

    private static final Logger log = LoggerFactory.getLogger(ZeebeWorkers.class);

    private final ZeebeClient zeebeClient;
    private final DocumentValidationService documentValidationService;
    private final CreditCheckService creditCheckService;
    private final AutoApprovalService autoApprovalService;

    public ZeebeWorkers(ZeebeClient zeebeClient,
                        DocumentValidationService documentValidationService,
                        CreditCheckService creditCheckService,
                        AutoApprovalService autoApprovalService) {
        this.zeebeClient = zeebeClient;
        this.documentValidationService = documentValidationService;
        this.creditCheckService = creditCheckService;
        this.autoApprovalService = autoApprovalService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openWorkers() {
        // Validate Documents worker
        zeebeClient.newWorker()
                .jobType("validate-docs")
                .handler((client, job) -> {
                    Map<String, Object> vars = job.getVariablesAsMap();
                    log.info("[validate-docs] Handling job key={}, vars={}", job.getKey(), vars);
                    Map<String, Object> updates = documentValidationService.validateDocuments(vars);
                    client.newCompleteCommand(job.getKey())
                            .variables(updates)
                            .send().join();
                })
                .name("validate-docs-worker")
                .timeout(Duration.ofSeconds(30))
                .open();

        // Check Credit worker (set default credit score if missing)
        zeebeClient.newWorker()
                .jobType("check-credit")
                .handler((client, job) -> {
                    Map<String, Object> vars = job.getVariablesAsMap();
                    Map<String, Object> updates = creditCheckService.checkCredit(vars);
                    client.newCompleteCommand(job.getKey())
                            .variables(updates)
                            .send().join();
                })
                .name("check-credit-worker")
                .timeout(Duration.ofSeconds(30))
                .open();

        // Auto Approve worker
        zeebeClient.newWorker()
                .jobType("auto-approve")
                .handler((client, job) -> {
                    Map<String, Object> vars = job.getVariablesAsMap();
                    Map<String, Object> updates = autoApprovalService.autoApprove(vars);
                    client.newCompleteCommand(job.getKey())
                            .variables(updates)
                            .send().join();
                })
                .name("auto-approve-worker")
                .timeout(Duration.ofSeconds(30))
                .open();

        // Manual Review worker (simulates a quick approval)
        zeebeClient.newWorker()
                .jobType("manual-review")
                .handler((client, job) -> {
                    Map<String, Object> vars = job.getVariablesAsMap();
                    log.info("[manual-review] job={}, vars={}", job.getKey(), vars);
                    client.newCompleteCommand(job.getKey())
                            .send().join();
                })
                .name("manual-review-worker")
                .timeout(Duration.ofSeconds(30))
                .open();

        log.info("Zeebe workers opened: validate-docs, check-credit, auto-approve, manual-review");
    }
}
