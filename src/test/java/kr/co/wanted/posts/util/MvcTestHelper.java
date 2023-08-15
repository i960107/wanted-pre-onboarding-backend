package kr.co.wanted.posts.util;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import kr.co.wanted.posts.config.jwt.JwtLoginFilter;
import kr.co.wanted.posts.domain.post.PostImageRepository;
import kr.co.wanted.posts.domain.post.PostRepository;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.domain.user.UserAuthorityRepository;
import kr.co.wanted.posts.domain.user.UserRepository;
import kr.co.wanted.posts.service.PostService;
import kr.co.wanted.posts.service.UserService;
import kr.co.wanted.posts.web.dto.UserSaveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.config.BeanIds;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
@Import(RestDocsConfiguration.class)
public class MvcTestHelper {
    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocs;

    @Autowired
    private ResourceLoader resourceLoader;

    protected ObjectMapper objectMapper;
    protected TokenBox tokenBox;

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected UserAuthorityRepository userAuthorityRepository;
    @Autowired
    protected PostImageRepository postImageRepository;
    @Autowired
    protected PostRepository postRepository;
    @Autowired
    protected PostService postService;

    @BeforeEach
    void setUp(
            final WebApplicationContext context,
            final RestDocumentationContextProvider provider
    ) throws Exception {
        DelegatingFilterProxy delegatingFilterProxy = new DelegatingFilterProxy();
        delegatingFilterProxy.init(new MockFilterConfig(
                context.getServletContext(),
                BeanIds.SPRING_SECURITY_FILTER_CHAIN));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation
                        .documentationConfiguration(provider)
                        .uris()
                        .withScheme("http")
                        .withHost("ec2-52-78-148-180.ap-northeast-2.compute.amazonaws.com")
                )
                .addFilter(delegatingFilterProxy)
                .alwaysDo(MockMvcResultHandlers.print())
                .alwaysDo(restDocs)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userAuthorityRepository.deleteAllInBatch();
        postImageRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        UserService userService = (UserService) context.getBean("userService");
        UserSaveRequest dto = objectMapper.readValue(readJson("/json/user/create.json"), UserSaveRequest.class);
        User user = userService.save(dto.toEntity());

        MvcResult result = mockMvc.perform(post("/login")
                .content(readJson("/json/user/login.json"))
        ).andReturn();
         tokenBox = new TokenBox(
                result.getResponse().getHeader(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME),
                result.getResponse().getHeader(JwtLoginFilter.REFRESH_TOKEN_HEADER_NAME),
                user
        );
    }

    protected String readJson(final String path) throws IOException {
        return new String(
                resourceLoader.getResource("classpath:" + path).getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
    }
}
