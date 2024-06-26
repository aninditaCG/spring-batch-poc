package org.sb.batch.poc.app.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.dsl.Sftp;

import java.io.File;

@Configuration
@EnableIntegration
@IntegrationComponentScan
public class SftpConfig {

    @Bean
    public DefaultSftpSessionFactory sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost("sftp.example.com");
        factory.setPort(22);
        factory.setUser("username");
        factory.setPassword("password");
        factory.setAllowUnknownKeys(true);
        return factory;
    }

    @Bean
    public IntegrationFlow sftpInboundFlow() {
        return IntegrationFlow.from(Sftp.inboundAdapter(sftpSessionFactory())
                                .preserveTimestamp(true)
                                .remoteDirectory("/remote/directory")
                                .localDirectory(new File("local/directory"))
                                .autoCreateLocalDirectory(true),
                        e -> e.poller(Pollers.fixedDelay(60000)))
                .handle(System.out::println)
                .get();
    }
}

