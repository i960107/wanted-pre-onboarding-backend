package kr.co.wanted.posts.util;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import kr.co.wanted.posts.config.jwt.JwtLoginFilter;
import kr.co.wanted.posts.domain.post.PostImageRepository;
import kr.co.wanted.posts.domain.post.PostRepository;
import kr.co.wanted.posts.domain.user.UserAuthorityRepository;
import kr.co.wanted.posts.domain.user.UserRepository;
import kr.co.wanted.posts.service.PostService;
import kr.co.wanted.posts.service.UserService;
import kr.co.wanted.posts.web.dto.UserLoginDto;
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
    protected UserTestHelper userTestHelper;
    protected PostTestHelper postTestHelper;

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
        this.userTestHelper = new UserTestHelper(userService);
        this.postTestHelper = new PostTestHelper(postService);

        this.postImageRepository.deleteAllInBatch();
        this.postRepository.deleteAllInBatch();
        this.userAuthorityRepository.deleteAllInBatch();
        this.userRepository.deleteAllInBatch();
    }


    protected TokenBox getAuthToken() throws Exception {
        userTestHelper.createUser("user");

        UserLoginDto userLoginDto = new UserLoginDto("user@email.com", "user1111", null);

        ResponseEntity response = testRestTemplate
                .exchange("/login", HttpMethod.POST, new HttpEntity<>(userLoginDto), Void.class);

        String authToken = response.getHeaders().get(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME).get(0);
        String refreshToken = response.getHeaders().get(JwtLoginFilter.REFRESH_TOKEN_HEADER_NAME).get(0);

        return new TokenBox(authToken, refreshToken);
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
