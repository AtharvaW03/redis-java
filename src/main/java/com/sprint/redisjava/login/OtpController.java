package com.sprint.redisjava.login;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/otp")
public class OtpController {

    private final StringRedisTemplate redis;

    public OtpController(StringRedisTemplate redis){
        this.redis=redis;
    }

    record OtpRequest(String phone){}

    record VerifyOtpRequest(
            String phone,
            String otp
    ){}

    private String otpKey(String phone){
        return "otp:" + phone;
    }

    @PostMapping()
    public ResponseEntity<?> getOtp(
            @RequestBody OtpRequest request
    ) {
        String phone = request.phone();

        String otp = String.valueOf(
                100000+new Random().nextInt(900000)
        );

        redis.opsForValue().set(
                otpKey(phone),
                otp,
                Duration.ofSeconds(30)
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "OTP sent",
                        "otp", otp
                )
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(
            @RequestBody VerifyOtpRequest request
    ){
        String savedOtp = redis.opsForValue().get(
                otpKey(request.phone())
        );

        if(savedOtp==null){
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "OTP expired or not found"
                    )
            );
        }

        if(!savedOtp.equals(request.otp())){
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Invalid OTP"
                    )
            );
        }

        redis.delete(otpKey(request.phone()));

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "OTP verified successfully"
                )
        );

    }

    @GetMapping("/{phone}/ttl")
    public ResponseEntity<Map<String, Long>> getTtl(
            @PathVariable String phone
    ){
        Long ttl = redis.getExpire(
                otpKey(phone)
        );

        return ResponseEntity.ok(
                Map.of(
                        "ttl", ttl
                )
        );
    }
}
