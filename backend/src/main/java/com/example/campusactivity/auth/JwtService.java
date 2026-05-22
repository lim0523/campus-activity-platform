package com.example.campusactivity.auth;

import com.example.campusactivity.exception.UnauthorizedException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expireSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret:campus-activity-demo-secret-change-me}") String secret,
            @Value("${app.jwt.expire-seconds:43200}") long expireSeconds
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expireSeconds = expireSeconds;
    }

    public String createToken(AuthenticatedUser user) {
        try {
            String header = encode(Map.of("alg", "HS256", "typ", "JWT"));
            long exp = Instant.now().getEpochSecond() + expireSeconds;
            String payload = encode(Map.of(
                    "userId", user.userId(),
                    "username", user.username(),
                    "realName", user.realName(),
                    "roleCode", user.roleCode(),
                    "exp", exp
            ));
            String signature = sign(header + "." + payload);
            return header + "." + payload + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("生成令牌失败", exception);
        }
    }

    public AuthenticatedUser parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new UnauthorizedException("无效的登录令牌");
            }
            String unsigned = parts[0] + "." + parts[1];
            if (!sign(unsigned).equals(parts[2])) {
                throw new UnauthorizedException("登录令牌校验失败");
            }
            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    }
            );
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > exp) {
                throw new UnauthorizedException("登录状态已过期");
            }
            return new AuthenticatedUser(
                    ((Number) payload.get("userId")).longValue(),
                    String.valueOf(payload.get("username")),
                    String.valueOf(payload.get("realName")),
                    String.valueOf(payload.get("roleCode"))
            );
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new UnauthorizedException("无效的登录令牌");
        }
    }

    private String encode(Map<String, Object> body) throws Exception {
        byte[] json = objectMapper.writeValueAsBytes(body);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signature = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }
}
