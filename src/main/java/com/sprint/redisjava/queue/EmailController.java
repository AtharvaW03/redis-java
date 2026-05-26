package com.sprint.redisjava.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/emails")
public class EmailController {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public EmailController(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    private static final String QUEUE_KEY = "queue:emails";

    record EmailRequest(
            String to,
            String subject,
            String body
    ){}

    record EmailJob(
            String to,
            String subject,
            String body,
            String createdAt
    ){}

    @PostMapping
    public ResponseEntity<?> queueEmail(
            @RequestBody EmailRequest request
    ) throws JsonProcessingException {

        EmailJob job = new EmailJob(
                request.to(),
                request.subject() != null
                        ? request.subject()
                        : "No subject",
                request.body() != null
                        ? request.body()
                        : "No content",
                Instant.now().toString()
        );

        String json = objectMapper.writeValueAsString(job);

        redis.opsForList().leftPush(
                QUEUE_KEY,
                json
        );

        return ResponseEntity.ok(
                Map.of(
                        "queued", true,
                        "job", job
                )
        );
    }

    @GetMapping("/process-one")
    public ResponseEntity<?> processOne()
            throws JsonProcessingException {

        String rawJob = redis.opsForList().rightPop(
                QUEUE_KEY
        );

        if (rawJob == null) {
            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "No jobs in queue"
                    )
            );
        }

        Object job = objectMapper.readValue(
                rawJob,
                Object.class
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Email sent",
                        "job", job
                )
        );
    }


}
