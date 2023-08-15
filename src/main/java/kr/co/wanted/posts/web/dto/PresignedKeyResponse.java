package kr.co.wanted.posts.web.dto;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
public class PresignedKeyResponse {
    private String presignedKey;
    private LocalDateTime expiresAt;

    @Builder
    public PresignedKeyResponse(String presignedKey, Date expiresAt) {
        this.presignedKey = presignedKey;
        this.expiresAt = Instant.ofEpochMilli(expiresAt.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
