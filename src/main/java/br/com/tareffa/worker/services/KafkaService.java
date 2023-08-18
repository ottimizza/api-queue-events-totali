package br.com.tareffa.worker.services;

import br.com.tareffa.worker.config.kafka.KafkaProperties;
import br.com.tareffa.worker.domain.commands.KafkaPayloadMessage;
import br.com.tareffa.worker.domain.commands.KafkaPayloadPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class KafkaService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProperties kafkaProperties;
    
    // PARAMS -> Obj para listener, Principal do request, Topico criado no Kafka
    public void saveMessage(String objeto, Principal principal, String prefixKafka) throws Exception{
        ObjectMapper mapper = new ObjectMapper();

        final KafkaPayloadMessage payload = KafkaPayloadMessage.builder()
            .principal(KafkaPayloadPrincipal.builder().name("Totali").build())
            .message(mapper.writeValueAsString(objeto))
            .build();

        final String KAFKA_PREFIX = kafkaProperties.getPrefix();
        final String KAFKA_TOPIC = KAFKA_PREFIX + prefixKafka;
        final String KAFKA_MESSAGE = mapper.writeValueAsString(payload);      
       	ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(KAFKA_TOPIC, KAFKA_MESSAGE);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                StringBuilder sb = new StringBuilder();
                sb.append("status=[success] ");
                sb.append("topic=[").append(KAFKA_TOPIC).append("] ");
                sb.append("message=[").append(KAFKA_MESSAGE).append("] ");
                sb.append("offset=[").append(result.getRecordMetadata().offset()).append("] ");

            }

            @Override
            public void onFailure(Throwable ex) {
                StringBuilder sb = new StringBuilder();
                sb.append("status=[failure] ");
                sb.append("topic=[").append(KAFKA_TOPIC).append("] ");
                sb.append("message=[").append(KAFKA_MESSAGE).append("] ");
                sb.append("reason=[").append(ex.getMessage()).append("] ");

            }
        });
    }
}