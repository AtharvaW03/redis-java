package com.sprint.redisjava.banner;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/banner")
public class BannerController {

    private final StringRedisTemplate redis;

    public BannerController(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private final String bannerKey = "app:banner";

    record BannerRequest(String message) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> setBanner(
            @RequestBody BannerRequest request
    ) {

        redis.opsForValue().set(
                bannerKey,
                request.message()
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", request.message()
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getBanner() {

        String message = redis.opsForValue().get(bannerKey);

        if (message == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", message
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Boolean>> deleteBanner() {

        redis.delete(bannerKey);

        return ResponseEntity.ok(
                Map.of(
                        "success", true
                )
        );
    }

    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> exists() {

        boolean exists = redis.hasKey(bannerKey);

        return ResponseEntity.ok(
                Map.of(
                        "exists", exists
                )
        );
    }
}