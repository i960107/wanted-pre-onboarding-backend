package kr.co.wanted.posts.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.time.Instant;
import java.util.Date;
import kr.co.wanted.posts.web.dto.PresignedKeyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PostImageService {
    private final String bucket;
    private final int KEY_EXPIRES_AFTER;
    @Autowired
    AmazonS3Client s3Client;

    public PostImageService(
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value(" ${cloud.aws.s3.key-time}") int keyTime
    ) {
        this.bucket = bucket;
        this.KEY_EXPIRES_AFTER = keyTime;
    }


    public PresignedKeyResponse getPresignedUrl(String filename) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, filename)
                .withMethod(HttpMethod.PUT)
                .withExpiration(Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond() + KEY_EXPIRES_AFTER)));
        return PresignedKeyResponse.builder()
                .expiresAt(request.getExpiration())
                .presignedKey(s3Client.generatePresignedUrl(request).toString())
                .build();
    }
}
