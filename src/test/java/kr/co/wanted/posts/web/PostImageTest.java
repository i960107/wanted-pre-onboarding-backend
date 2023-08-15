package kr.co.wanted.posts.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import kr.co.wanted.posts.config.jwt.JwtLoginFilter;
import kr.co.wanted.posts.util.TestHelper;
import kr.co.wanted.posts.util.TokenBox;
import kr.co.wanted.posts.web.dto.PresignedKeyResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class PostImageTest extends TestHelper {
    @DisplayName("1.presigned key를 발급받는다.")
    @Test
    void test_presignedkey() throws Exception {
        TokenBox tokenBox = createUserAndGetAuthToken();

        String fileName = "file1";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken());

        ResponseEntity<PresignedKeyResponse> response = testRestTemplate.exchange(
                uri("/api/images?fileName=" + fileName),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PresignedKeyResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getPresignedKey());
        assertTrue(response.getBody().getExpiresAt().isAfter(LocalDateTime.now()));
    }
}
