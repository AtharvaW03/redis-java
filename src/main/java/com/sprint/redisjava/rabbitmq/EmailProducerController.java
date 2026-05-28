package com.sprint.redisjava.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/rabbitmq/emails")
public class EmailProducerController {
    private final RabbitTemplate rabbitTemplate;

    public EmailProducerController(
            RabbitTemplate rabbitTemplate
    ){
        this.rabbitTemplate = rabbitTemplate;
    }

    record EmailRequest(
            String to,
            String name
    ){}

    record EmailJob(
            String to,
            String name,
            String createdAt
    ){}

    @PostMapping
    public ResponseEntity<?> sendWelcomeEmail(
            @RequestBody EmailRequest request
    ){
        EmailJob job = new EmailJob(
                request.to(),
                request.name() != null ? request.name() : "Learner",
                Instant.now().toString()
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.EMAIL_QUEUE,
                job
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Welcome email job added to queue"
                )
        );
    }
}
