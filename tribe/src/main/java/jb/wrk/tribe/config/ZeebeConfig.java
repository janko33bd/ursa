package jb.wrk.tribe.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class ZeebeConfig {

    private static final Logger log = LoggerFactory.getLogger(ZeebeConfig.class);

    @Value("${camunda.client.zeebe.gateway-url:127.0.0.1:26500}")
    private String gatewayAddress;

    @Value("${camunda.client.zeebe.security.plaintext:true}")
    private boolean plaintext;

    @Value("${camunda.client.startup.enabled:true}")
    private boolean startupEnabled;

    @Bean(destroyMethod = "close")
    public ZeebeClient zeebeClient() {
        ZeebeClientBuilder builder = ZeebeClient.newClientBuilder()
                .gatewayAddress(gatewayAddress);
        if (plaintext) {
            builder.usePlaintext();
        }
        ZeebeClient client = builder.build();
        log.info("Zeebe client created. Gateway={}, plaintext={}.", gatewayAddress, plaintext);
        return client;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void deployWorkflowOnStartup() {
        if (!startupEnabled) {
            log.info("Camunda startup deploy disabled by property.");
            return;
        }
        try {
            // Deploy BPMN from classpath
            DeploymentEvent deployment = zeebeClient().newDeployResourceCommand()
                    .addResourceFromClasspath("loan-approval.bpmn")
                    .send()
                    .join();
            log.info("Deployed processes: {}", deployment.getProcesses());
        } catch (Exception ex) {
            log.warn("Failed to deploy BPMN 'loan-approval.bpmn' on startup: {}", ex.getMessage());
        }
    }
}
