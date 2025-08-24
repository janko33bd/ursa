package jb.wrk.tribe.demo.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Camunda 8 compatible helper service used by ZeebeWorkers to perform document validation
 * and provide process variable updates.
 */
@Component("documentValidationService")
public class DocumentValidationService {
    private static final Logger log = LoggerFactory.getLogger(DocumentValidationService.class);

    /**
     * Perform a mock validation and return variables to update in the workflow.
     */
    public Map<String, Object> validateDocuments(Map<String, Object> variables) {
        Object processInstanceId = variables.get("processInstanceId");
        log.info("Validating documents for process: {} vars={}", processInstanceId, variables);

        Map<String, Object> updates = new HashMap<>();
        updates.put("documentsValid", true);
        updates.put("validationTimestamp", LocalDateTime.now().toString());
        return updates;
    }
}

