package co.com.requestloancrediyareactivo.consumer.config;

import co.com.requestloancrediyareactivo.consumer.CapacidadProps;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
@EnableConfigurationProperties(CapacidadProps.class)
public class RestConsumerConfig {

    @Bean
    WebClient capacidadClient(CapacidadProps p) {
        return WebClient.builder().baseUrl(p.getBaseUrl()).build();
    }

}
