package jb.wrk.tribe.demo.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Camunda 8 compatible helper service used by ZeebeWorkers to perform credit checks.
 */
@Component("creditCheckService")
public class CreditCheckService {

    private static final Logger log = LoggerFactory.getLogger(CreditCheckService.class);

    /**
     * Perform a mock credit check. If a creditScore is already provided, use it; otherwise set a default.
     */
    public Map<String, Object> checkCredit(Map<String, Object> variables) {
        Object loanApplicationId = variables.get("loanApplicationId");
        log.info("Checking credit score for loan application: {} vars={}", loanApplicationId, variables);

        Integer creditScore = null;
        Object v = variables.get("creditScore");
        if (v instanceof Number) {
            creditScore = ((Number) v).intValue();
        }
        if (creditScore == null) {
            creditScore = 750; // default mock score
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("creditScore", creditScore);
        updates.put("creditCheckComplete", true);
        updates.put("creditCheckTimestamp", LocalDateTime.now().toString());
        return updates;
    }
}

