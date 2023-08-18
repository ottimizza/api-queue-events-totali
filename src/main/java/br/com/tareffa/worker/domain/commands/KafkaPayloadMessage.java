package br.com.tareffa.worker.domain.commands;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class KafkaPayloadMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private KafkaPayloadPrincipal principal;

    private String message;

}