package br.com.tareffa.worker.controllers;

import br.com.tareffa.worker.services.KafkaService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/totali/v1")
public class MessagesController {
    
    @Autowired
    KafkaService kafkaService;

    @PostMapping("/teste")
    public ResponseEntity<?> testeEnvioListener(@RequestBody String objeto, Principal principal) throws Exception {
        kafkaService.saveMessage(objeto, principal, "envia.mensagem");
        return ResponseEntity.noContent().build();
    }

}