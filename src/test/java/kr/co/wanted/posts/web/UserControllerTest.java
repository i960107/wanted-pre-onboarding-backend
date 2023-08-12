package kr.co.wanted.posts.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import kr.co.wanted.posts.config.jwt.JwtLoginFilter;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.exception.BaseException.BaseExceptions;
import kr.co.wanted.posts.util.TestHelper;
import kr.co.wanted.posts.util.TokenBox;
import kr.co.wanted.posts.web.dto.PostSaveUpdateRequestDto;
import kr.co.wanted.posts.web.dto.UserLoginDto;
import kr.co.wanted.posts.web.dto.UserSaveRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class UserControllerTest extends TestHelper {
    @DisplayName("1.로그인을 하면 token을 두개 받는다.")
    @Test
    void test_login() throws Exception {
        //given
        userTestHelper.createUser("user");

        UserLoginDto userLoginDto = new UserLoginDto("user@email.com", "user1111", null);

        //when
        String path = "/login";

        HttpEntity<UserLoginDto> request = new HttpEntity<>(userLoginDto);
        ResponseEntity<Void> response = testRestTemplate.exchange(uri(path), HttpMethod.POST, request, Void.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().get(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME));
        assertNotNull(response.getHeaders().get(JwtLoginFilter.REFRESH_TOKEN_HEADER_NAME));

//        MvcResult result = mockMvc.perform(post(path)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userLoginDto)))
//                .andExpect(header().exists("auth_token"))
//                .andExpect(header().exists("refresh_token"))
//                .andExpect(status().is3xxRedirection())
//                .andReturn();
//
    }

    @DisplayName("2.잘못된 형식으로 로그인을 시도한다.")
    @Test
    void test_login_fail() throws Exception {
        userTestHelper.createUser("user");

        String path = "/login";

        UserLoginDto invalidEmailLoginDto = new UserLoginDto("user", "user1111", null);

        HttpEntity<UserLoginDto> request = new HttpEntity<>(invalidEmailLoginDto);
        ResponseEntity<String> response = testRestTemplate.exchange(uri(path), HttpMethod.POST, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(BaseExceptions.INVALID_EMAIL.getMessage()));

        UserLoginDto invalidPasswordRequestDto = new UserLoginDto("user@email.com", "123", null);

        request = new HttpEntity<>(invalidPasswordRequestDto);
        response = testRestTemplate.exchange(uri(path), HttpMethod.POST, request, String.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(BaseExceptions.INVALID_PASSWORD.getMessage()));
    }


    @DisplayName("2. 만료된 토큰을 사용한다.")
    @Test
    void test_invalid_token() throws Exception {
        TokenBox tokenBox = getAuthToken();

        Thread.sleep(3000);

        PostSaveUpdateRequestDto requestDto = objectMapper
                .readValue(readJson("/json/post/post.json"), PostSaveUpdateRequestDto.class);

        URI uri = uri("/api/posts");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken());
        HttpEntity<PostSaveUpdateRequestDto> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<Void> response = testRestTemplate
                .exchange(uri, HttpMethod.POST, request, Void.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @DisplayName("3. 만료된 토큰을 갱신한다.")
    @Test
    void test_refresh_auth_token() throws Exception {
        TokenBox tokenBox = getAuthToken();

        Thread.sleep(3000);

        UserLoginDto loginDto = new UserLoginDto(null, null, tokenBox.getRefreshToken());

        URI uri = uri("/login");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<UserLoginDto> request = new HttpEntity<>(loginDto, headers);

        ResponseEntity<Void> response = testRestTemplate
                .exchange(uri, HttpMethod.POST, request, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME));
        assertTrue(response.getHeaders().containsKey(JwtLoginFilter.REFRESH_TOKEN_HEADER_NAME));
    }

    @DisplayName("4. 유저를 등록한다.")
    @Test
    void test_save() throws IOException, URISyntaxException {
        String path = "/api/users";
        UserSaveRequestDto requestDto = objectMapper
                .readValue(readJson("/json/user/post.json"), UserSaveRequestDto.class);

        ResponseEntity<Void> response = testRestTemplate
                .exchange(uri(path), HttpMethod.POST, new HttpEntity<>(requestDto), Void.class);
        assertEquals(HttpStatus.TEMPORARY_REDIRECT, response.getStatusCode());
        assertEquals("/", response.getHeaders().getLocation().toString());
    }

    @DisplayName("5. 중복된 이메일로 유저를 등록한다.")
    @Test
    void test_user_create_fail_when_email_is_duplicated() throws IOException, URISyntaxException {
        String path = "/api/users";
        UserSaveRequestDto requestDto = objectMapper
                .readValue(readJson("/json/user/post.json"), UserSaveRequestDto.class);

        testRestTemplate
                .exchange(uri(path), HttpMethod.POST, new HttpEntity<>(requestDto), Void.class);

        ResponseEntity<BaseException> response = testRestTemplate
                .exchange(uri(path), HttpMethod.POST, new HttpEntity<>(requestDto), BaseException.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(BaseExceptions.USER_EMAIL_DUPLICATE.getMessage()));
    }

    @DisplayName("6. 이메일 형식이 잘못되면 유저 등록에 실패한다.")
    @Test
    void test_user_create_fail_when_email_is_not_valid() throws IOException, URISyntaxException {
        String path = "/api/users";
        UserSaveRequestDto requestDto = objectMapper
                .readValue(readJson("/json/user/post-invalid-email.json"), UserSaveRequestDto.class);

        ResponseEntity<String> response = testRestTemplate
                .exchange(uri(path), HttpMethod.POST, new HttpEntity<>(requestDto), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("email"));
    }

    @DisplayName("7. 비밀번호가 8자리가 되지 않으면 유저 등록에 실패한다.")
    @Test
    void test_user_create_fail_when_password_is_short() throws IOException, URISyntaxException {
        String path = "/api/users";
        UserSaveRequestDto requestDto = objectMapper
                .readValue(readJson("/json/user/post-invalid-password.json"), UserSaveRequestDto.class);

        ResponseEntity<String> response = testRestTemplate
                .exchange(uri(path), HttpMethod.POST, new HttpEntity<>(requestDto), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("password"));
    }

}