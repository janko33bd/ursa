package jb.wrk.tribe.demo.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@Validated
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:80", "http://localhost"})
public class LoanApplicationController {

    private final ZeebeClient zeebeClient;

    public LoanApplicationController(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startLoanProcess(@RequestParam(name = "creditScore", required = false) Integer creditScore) {
        Map<String, Object> vars = new HashMap<>();
        if (creditScore != null) {
            vars.put("creditScore", creditScore);
        }
        ProcessInstanceEvent event = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("loanApprovalProcess")
                .latestVersion()
                .variables(vars)
                .send()
                .join();
        Map<String, Object> resp = new HashMap<>();
        resp.put("processInstanceKey", event.getProcessInstanceKey());
        resp.put("bpmnProcessId", "loanApprovalProcess");
        resp.put("version", event.getVersion());
        resp.put("variables", vars);
        return ResponseEntity.ok(resp);
    }
}

