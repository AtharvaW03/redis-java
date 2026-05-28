package com.sprint.redisjava.leaderboard;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final StringRedisTemplate redis;

    public LeaderboardController(
            StringRedisTemplate redis
    ) {
        this.redis = redis;
    }

    private static final String LEADERBOARD_KEY =
            "leaderboard";

    record ScoreRequest(

            @NotBlank(message = "User id is required")
            String id,

            @Positive(message = "Points must be positive")
            int points

    ) {}

    private String viewsKey(String id) {
        return "views:" + id;
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<?> incrementViews(
            @PathVariable String id
    ) {

        Long currentViews =
                redis.opsForValue().increment(
                        viewsKey(id)
                );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Incremented user " +
                                id +
                                " counter by 1",

                        "currentViews",
                        currentViews
                )
        );
    }

    @PostMapping("/score")
    public ResponseEntity<?> incrementScore(
            @Valid @RequestBody ScoreRequest request
    ) {

        Double updatedScore =
                redis.opsForZSet().incrementScore(
                        LEADERBOARD_KEY,
                        request.id(),
                        request.points()
                );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Incremented user " +
                                request.id() +
                                " points by " +
                                request.points(),

                        "newScore",
                        updatedScore
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getLeaderboard() {

        Set<ZSetOperations.TypedTuple<String>>
                leaderboard =
                redis.opsForZSet()
                        .reverseRangeWithScores(
                                LEADERBOARD_KEY,
                                0,
                                9
                        );

        return ResponseEntity.ok(
                Map.of(
                        "leaderboard",
                        leaderboard
                )
        );
    }

    @GetMapping("/{userId}/rank")
    public ResponseEntity<?> getRank(
            @PathVariable String userId
    ) {

        Long rank =
                redis.opsForZSet()
                        .reverseRank(
                                LEADERBOARD_KEY,
                                userId
                        );

        if(rank == null) {
            return ResponseEntity
                    .status(404)
                    .body(
                            Map.of(
                                    "error",
                                    "User not found on leaderboard"
                            )
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                        "userId",
                        userId,

                        "rank",
                        rank
                )
        );
    }
}