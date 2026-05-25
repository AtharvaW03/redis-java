package com.sprint.redisjava.caching;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class CacheController {
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public CacheController(StringRedisTemplate redis, ObjectMapper objectMapper){
        this.redis=redis;
        this.objectMapper=objectMapper;
    }

    record UserRequest(
            String name,
            int age
    ){}

    private String jsonKey(String id){
        return "user:" + id + ":json";
    }

    private String hashKey(String id){
        return "user:" + id + ":hash";
    }

    // JSON storage

    @PostMapping("/{id}/json")
    public ResponseEntity<?> saveJson(
            @PathVariable String id,
            @RequestBody UserRequest request
    ) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(request);

        redis.opsForValue().set(
                jsonKey(id),
                json
        );

        return ResponseEntity.ok(
                Map.of(
                        "savedAs", "json"
                )
        );
    }

    @GetMapping("/{id}/json")
    public ResponseEntity<?> getJson(
            @PathVariable String id
    ) throws JsonProcessingException{
        String raw = redis.opsForValue().get(
                jsonKey(id)
        );

        if(raw == null){
            return ResponseEntity.notFound().build();
        }

        Object user = objectMapper.readValue(
                raw,
                Object.class
        );

        return ResponseEntity.ok(
                Map.of(
                        "user", user
                )
        );
    }

    // Hash storage

    @PostMapping("/{id}/hash")
    public ResponseEntity<?> saveHash(
            @PathVariable String id,
            @RequestBody UserRequest request
    ){
        Map<String, String> userMap = new HashMap<>();

        userMap.put("name", request.name());
        userMap.put("age", String.valueOf(request.age()));

        redis.opsForHash().putAll(
                hashKey(id),
                userMap
        );

        return ResponseEntity.ok(
                Map.of(
                        "savedAs", "hash"
                )
        );
    }

    @GetMapping("/{id}/hash")
    public ResponseEntity<?> getHash(
            @PathVariable String id
    ){
        Map<Object, Object> user = redis.opsForHash().entries(
                hashKey(id)
        );

        return ResponseEntity.ok(
                Map.of(
                        "user", user
                )
        );

    }

}
