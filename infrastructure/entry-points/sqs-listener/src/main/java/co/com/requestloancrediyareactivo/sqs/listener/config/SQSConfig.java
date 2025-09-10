package co.com.requestloancrediyareactivo.sqs.listener.config;

import co.com.requestloancrediyareactivo.sqs.listener.SQSProcessor;
import co.com.requestloancrediyareactivo.sqs.listener.helper.SQSListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.net.URI;
import java.util.function.Function;

@Slf4j
@Configuration
@EnableConfigurationProperties(SQSProperties.class)
public class SQSConfig {
    @Bean
    public SQSListener sqsListener(
            @Qualifier("listenerSqsClient")SqsAsyncClient client,
                                   SQSProperties properties,
                                   SQSProcessor processor) {
        return SQSListener.builder()
                .client(client)
                .properties(properties)
                .processor(processor)
                .build()
                .start();
    }

    @Bean(name = "listenerSqsClient")
    public SqsAsyncClient configSqs(
            SQSProperties properties,
            @Value("${aws.region:us-east-1}") String region,
            @Value("${aws.accessKeyId}") String accessKey,
            @Value("${aws.secretAccessKey}") String secretKey) {


        var builder = SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));



        log.info("---[SQS]---", region);
        return builder.build();
    }

}
