package br.com.tareffa.worker.config.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Getter;

import com.github.jkutner.EnvKeyStore;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;

@Getter
@Component
@PropertySource("classpath:heroku-kafka.yml")
@ConfigurationProperties(prefix = "heroku-kafka-config")
public class KafkaProperties {

    @Value("${url}")
    private String url;

    @Value("${prefix}")
    private String prefix;

    @Value("${client-cert}")
    private String clientCert;

    @Value("${client-cert-key}")
    private String clientCertKey;

    @Value("${trusted-cert}")
    private String trustedCert;

    public Properties buildDefaults() {
        Properties properties = new Properties();
        List<String> hostPorts = new ArrayList<String>();

        for (String url : this.getUrl().split(",")) {
            try {
                URI uri = new URI(url);
                hostPorts.add(format("%s:%d", uri.getHost(), uri.getPort()));

                switch (uri.getScheme()) {
                    case "kafka":
                        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
                        break;
                    case "kafka+ssl":
                        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
                        properties.put("ssl.endpoint.identification.algorithm", "");

                        try {
                            EnvKeyStore envTrustStore = EnvKeyStore.createWithRandomPassword("KAFKA_TRUSTED_CERT");
                            EnvKeyStore envKeyStore = EnvKeyStore.createWithRandomPassword("KAFKA_CLIENT_CERT_KEY",
                                    "KAFKA_CLIENT_CERT");

                            File trustStore = envTrustStore.storeTemp();
                            File keyStore = envKeyStore.storeTemp();

                            properties.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, envTrustStore.type());
                            properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStore.getAbsolutePath());
                            properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, envTrustStore.password());
                            properties.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, envKeyStore.type());
                            properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStore.getAbsolutePath());
                            properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, envKeyStore.password());
                            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                            properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                        } catch (Exception e) {
                            throw new RuntimeException("There was a problem creating the Kafka key stores", e);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException(format("unknown scheme; %s", uri.getScheme()));
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, String.join(",", hostPorts));
        return properties;
    }

}
