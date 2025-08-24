package jb.wrk.tribe.demo.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Camunda 8 compatible helper service used by ZeebeWorkers for auto-approval.
 * This mock version avoids repository/notification dependencies and returns variables only.
 */
@Component("autoApprovalService")
public class AutoApprovalService {

    private static final Logger log = LoggerFactory.getLogger(AutoApprovalService.class);

    /**
     * Mock auto-approval. In a real impl, update DB and notify. Here we only return variables.
     */
    public Map<String, Object> autoApprove(Map<String, Object> variables) {
        Object loanApplicationId = variables.get("loanApplicationId");
        log.info("Auto-approving loan application: {} vars={}", loanApplicationId, variables);

        Map<String, Object> updates = new HashMap<>();
        updates.put("approvalStatus", "APPROVED");
        return updates;
    }
}

