package kr.co.wanted.posts.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import kr.co.wanted.posts.domain.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private final Algorithm KEY;
    public final int TOKEN_EXPIRES_AFTER;
    public final int REFRESH_TOKEN_EXPIRES_AFTER;

    public JwtUtil(
            @Value("${jwt.secretkey}") String key,
            @Value("${jwt.auth-token-time}") int authTime,
            @Value("${jwt.refresh-token-time}") int refreshTime
    ) {
        this.KEY = Algorithm.HMAC256(key);
        this.TOKEN_EXPIRES_AFTER = authTime;
        this.REFRESH_TOKEN_EXPIRES_AFTER = refreshTime;
    }


    public String makeAuthToken(User user) {
        return JWT.create()
                .withSubject(String.valueOf(user.getId()))
                .withClaim("exp", Instant.now().getEpochSecond() + TOKEN_EXPIRES_AFTER)
                .sign(KEY);
    }

    public String makeRefreshToken(User user) {
        return JWT.create()
                .withSubject(String.valueOf(user.getId()))
                .withClaim("exp", Instant.now().getEpochSecond() + REFRESH_TOKEN_EXPIRES_AFTER)
                .sign(KEY);
    }

    public VerifyResult verify(String token) {
        try {
            DecodedJWT verified = JWT.require(KEY)
                    .build()
                    .verify(token);
            return VerifyResult
                    .builder()
                    .success(true)
                    .userId(Long.parseLong(verified.getSubject()))
                    .build();
        } catch (Exception exception) {
            DecodedJWT decoded = JWT.decode(token);
            return VerifyResult
                    .builder()
                    .success(false)
                    .userId(Long.parseLong(decoded.getSubject()))
                    .build();
        }
    }
}
