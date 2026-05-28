package com.sprint.redisjava.notification;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/notification")
public class NotificationPublisherController {

    private final StringRedisTemplate redis;

    public NotificationPublisherController(StringRedisTemplate redis){
        this.redis = redis;
    }


    private static final String CHANNEL = "notifications";

    @PostMapping
    public ResponseEntity<?> publish(
            @RequestBody Map<String, String> request
    ) {
        Map<String, String> payload = Map.of(
                "title",
                request.getOrDefault(
                        "title",
                        "Default Title"
                ),
                "createdAt",
                Instant.now().toString()
        );

        redis.convertAndSend(
                CHANNEL,
                payload.toString()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Notification published"
                )
        );
    }
}
