package kr.co.wanted.posts.util;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import kr.co.wanted.posts.config.jwt.JwtLoginFilter;
import kr.co.wanted.posts.domain.post.Post;
import kr.co.wanted.posts.domain.post.PostImageRepository;
import kr.co.wanted.posts.domain.post.PostRepository;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.domain.user.UserAuthorityRepository;
import kr.co.wanted.posts.domain.user.UserRepository;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.service.PostService;
import kr.co.wanted.posts.service.UserService;
import kr.co.wanted.posts.web.dto.PostSaveUpdateRequest;
import kr.co.wanted.posts.web.dto.UserLoginRequest;
import kr.co.wanted.posts.web.dto.UserSaveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestHelper {
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @LocalServerPort
    protected int port;

    @Autowired
    protected ResourceLoader resourceLoader;

    @Autowired
    protected UserService userService;
    @Autowired
    protected PostService postService;

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected UserAuthorityRepository userAuthorityRepository;
    @Autowired
    protected PostRepository postRepository;
    @Autowired
    protected PostImageRepository postImageRepository;


    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();

        this.postImageRepository.deleteAllInBatch();
        this.postRepository.deleteAllInBatch();
        this.userAuthorityRepository.deleteAllInBatch();
        this.userRepository.deleteAllInBatch();
    }

    protected User createUser(String name) throws BaseException {
        return userService.save(User.builder()
                .name(name)
                .password(name + "111111")
                .nickName(name + "love")
                .email(name + "@email.com")
                .build());
    }

    protected TokenBox createUserAndGetAuthToken() throws Exception {
        //create
        UserSaveRequest saveRequestDto = objectMapper.readValue(
                readJson("/json/user/create.json"),
                UserSaveRequest.class);
        User user = userService.save(saveRequestDto.toEntity());
        //login
        UserLoginRequest loginDto = objectMapper.readValue(
                readJson("/json/user/login.json"),
                UserLoginRequest.class);

        ResponseEntity<Void> response = testRestTemplate.exchange(
                uri("/login"),
                HttpMethod.POST,
                new HttpEntity<>(loginDto),
                Void.class);

        String authToken = response.getHeaders().get(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME).get(0);
        String refreshToken = response.getHeaders().get(JwtLoginFilter.REFRESH_TOKEN_HEADER_NAME).get(0);
        return new TokenBox(authToken, refreshToken, user);
    }

    protected Post createPost(Long authorId) throws Exception {
        URI uri = uri("/api/posts");
        PostSaveUpdateRequest createRequestDto = objectMapper
                .readValue(readJson("/json/post/create.json"), PostSaveUpdateRequest.class);
        return postService.save(authorId, createRequestDto);
    }

    protected String readJson(final String path) throws IOException {
        return new String(
                resourceLoader.getResource("classpath:" + path).getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
    }

    protected URI uri(String path) throws URISyntaxException {
        return new URI(format("http://localhost:%d%s", port, path));
    }
}
