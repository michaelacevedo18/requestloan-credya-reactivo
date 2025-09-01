package co.com.requestloancrediyareactivo.r2dbc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Slf4j
@Configuration
public class AwsSQSConfig {
    @Value("${aws.region}")
    private String region;

    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Value("${aws.queue}")
    private String queueUrl;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        log.info("✅ Inicializando cliente SQS con región: {}", region);

        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(extractBaseUrl(queueUrl)))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                ))
                .build();
    }

    private String extractBaseUrl(String fullUrl) {
        // Convierte: http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/cola-pruebas
        // En:        http://sqs.us-east-1.localhost.localstack.cloud:4566
        int idx = fullUrl.indexOf("/000000000000/");
        return idx != -1 ? fullUrl.substring(0, idx) : fullUrl;
    }
}
