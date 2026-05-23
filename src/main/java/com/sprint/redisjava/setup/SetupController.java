package com.sprint.redisjava.setup;

import com.mongodb.client.MongoClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/setup")
public class SetupController {

    private final StringRedisTemplate redis;
    private final MongoClient mongo;

    public SetupController(StringRedisTemplate redis, MongoClient mongo){
        this.redis=redis;
        this.mongo=mongo;
    }

    @GetMapping("/redis")
    public Map<String, String> redisPing(){
        redis.opsForValue().set("ping", "pong");
        return Map.of("redis", redis.opsForValue().get("ping"));
    }

    @GetMapping("/mongo")
    public Map<String, String> mongoPing(){
        String dbName = mongo.listDatabaseNames().first();
        return Map.of("mongo","connected",
                "database", dbName == null ? "unknown" : dbName
        );
    }
}
