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
    private final Algorithm key;
    public final int TOKEN_EXPIRES_AFTER = 1000 * 2; //1분
    public final int REFRESH_TOKEN_EXPIRES_AFTER = 1000 * 60; //1분

    public JwtUtil(@Value("${jwt.secretkey}") String key) {
        this.key = Algorithm.HMAC256(key);
    }


    public String makeAuthToken(User user) {
        return JWT.create()
                .withSubject(String.valueOf(user.getId()))
                .withClaim("exp", Instant.now().getEpochSecond() + TOKEN_EXPIRES_AFTER)
                .sign(key);
    }

    public String makeRefreshToken(User user) {
        return JWT.create()
                .withSubject(String.valueOf(user.getId()))
                .withClaim("exp", Instant.now().getEpochSecond() + REFRESH_TOKEN_EXPIRES_AFTER)
                .sign(key);
    }

    public VerifyResult verify(String token) {
        try {
            DecodedJWT verified = JWT.require(key)
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
