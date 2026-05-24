package com.sprint.redisjava.banner;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/banner")
public class BannerController {
    private final StringRedisTemplate redis;

    public BannerController (StringRedisTemplate redis){
        this.redis = redis;
    }


    record BannerRequest(String message){}


    @PostMapping("/{key}")
    public ResponseEntity<?> setBanner(
            @PathVariable String key,
            @RequestBody BannerRequest request
    ){
        redis.opsForValue().set(
                key,
                request.message()
        );
        return ResponseEntity.ok(
                Map.of(
                "success", true
                )
        );
    }

    @GetMapping("/{key}")
    public ResponseEntity<Map<String, String>> getBanner(
            @PathVariable String key
    ){
        String message = redis.opsForValue().get(key);
        if(message==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, Boolean>> deleteBanner(
            @PathVariable String key
    ){
        redis.delete(key);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/exists/{key}")
    public ResponseEntity<Map<String, Boolean>> exists(
            @PathVariable String key
    ){
        Boolean exists = redis.hasKey(key);
        return ResponseEntity.ok(Map.of(
                "exists",
                Boolean.TRUE.equals(exists)
        ));
    }
}