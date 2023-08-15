package kr.co.wanted.posts.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
    private final String ACCESS_KEY;
    private final String SECRET_KEY;
    private final String REGION;

    public S3Config(
            @Value("${cloud.aws.region.static}") String region,
            @Value("${cloud.aws.credentials.access-key}") String accesskey,
            @Value("${cloud.aws.credentials.secret-key}") String secretKey
    ) {
        this.ACCESS_KEY = accesskey;
        this.SECRET_KEY = secretKey;
        this.REGION = region;
    }

    @Bean
    public AmazonS3 amazonS3Client() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                ACCESS_KEY,
                SECRET_KEY
        );
        return AmazonS3ClientBuilder.standard()
                .withRegion(REGION)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

}
