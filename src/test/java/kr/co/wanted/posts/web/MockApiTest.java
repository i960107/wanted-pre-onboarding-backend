package kr.co.wanted.posts.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import kr.co.wanted.posts.config.jwt.JwtLoginFilter;
import kr.co.wanted.posts.domain.post.Post;
import kr.co.wanted.posts.service.UserService;
import kr.co.wanted.posts.util.MvcTestHelper;
import kr.co.wanted.posts.util.PostTestHelper;
import kr.co.wanted.posts.web.dto.PostDetailResponse;
import kr.co.wanted.posts.web.dto.PostListResponse;
import kr.co.wanted.posts.web.dto.PostSaveUpdateRequest;
import kr.co.wanted.posts.web.dto.PresignedKeyResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class MockApiTest extends MvcTestHelper {
    @Autowired
    private UserService userService;

    @DisplayName("1. 사용자 회원가입")
    @Test
    void test_create_user() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .content(readJson("/json/user/create2.json")))
                .andExpect(status().isFound())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("email").description("이메일 @ 필수 포함"),
                                fieldWithPath("nickName").description("닉네임"),
                                fieldWithPath("password").description("패스워드 최소 8자리")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("새로 생성된 리소스 주소")
                        )
                ));
    }

    @DisplayName("2. 사용자 로그인")
    @Test
    void test_login() throws Exception {
        mockMvc.perform(post("/login")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .content(readJson("/json/user/login.json")))
                .andExpect(status().isOk())
                .andExpect(header().exists(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME))
                .andExpect(header().exists(JwtLoginFilter.REFRESH_TOKEN_HEADER_NAME))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("email").description("이메일 @ 필수 포함"),
                                fieldWithPath("password").description("패스워드 최소 8자리")
                        ),
                        responseHeaders(
                                headerWithName(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME)
                                        .description("jwt 인증 토큰 유효기간 5분"),
                                headerWithName(JwtLoginFilter.REFRESH_TOKEN_HEADER_NAME)
                                        .description("jwt 리프레시 토큰 유효기간 1시간")
                        )
                ));
    }

    @DisplayName("3. 토큰 리프레시")
    @Test
    void test_refresh_token() throws Exception {
        mockMvc.perform(post("/login")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .content(String.format("{\n"
                                        + "\"refreshToken\": \"%s\"\n"
                                        + "}",
                                tokenBox.getRefreshToken()
                        )))
                .andExpect(status().isOk())
                .andExpect(header().exists(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME))
                .andExpect(header().exists(JwtLoginFilter.REFRESH_TOKEN_HEADER_NAME))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("refreshToken").description("jwt 리프레시 토큰")
                        ),
                        responseHeaders(
                                headerWithName(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME)
                                        .description("jwt 인증 토큰 유효기간 5분"),
                                headerWithName(JwtLoginFilter.REFRESH_TOKEN_HEADER_NAME)
                                        .description("jwt 리프레시 토큰 유효기간 1시간")
                        )
                ));
    }

    @DisplayName("4. aws s3 presignedkey 받기")
    @Test
    void test_presigned_key() throws Exception {
        mockMvc.perform(get("/api/images")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken())
                        .param("fileName", "testfile1.jpg"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    PresignedKeyResponse dto = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            PresignedKeyResponse.class);
                    assertNotNull(dto.getPresignedKey());
                    assertTrue(dto.getExpiresAt().isAfter(LocalDateTime.now()));
                })
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME).description("유효한 jwt 인증 토큰")
                        ),
                        responseFields(
                                fieldWithPath("presignedKey")
                                        .description("s3 파일 업로드를 위한 presignedKey 유효기간 2분"),
                                fieldWithPath("expiresAt")
                                        .description("만료시간")
                        )
                ));
    }

    @DisplayName("5. post 생성")
    @Test
    void test_create_post() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken())
                        .content(readJson("/json/post/create.json")))
                .andExpect(status().isFound())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME).description("유효한 jwt 인증 토큰")
                        ),
                        requestFields(
                                fieldWithPath("title").description("타이틀"),
                                fieldWithPath("content").description("내용"),
                                fieldWithPath("imageFileNames").description("이미지 파일 이름 리스트"),
                                fieldWithPath("thumbnail").description("썸네일 파일 이름")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("새로 생성된 리소스 주소")
                        )
                ));
    }

    @DisplayName("6. post 수정")
    @Test
    void test_update_post() throws Exception {
        PostSaveUpdateRequest requestDto = objectMapper.readValue(
                readJson("/json/post/create.json"),
                PostSaveUpdateRequest.class);
        Post post = postService.save(tokenBox.getUserId(), requestDto);

        mockMvc.perform(put("/api/posts/{postId}", post.getId())
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken())
                        .content(readJson("/json/post/update.json")))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME).description("유효한 jwt 인증 토큰")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("수정할 포스트 id")
                        ),
                        requestFields(
                                fieldWithPath("title").description("타이틀"),
                                fieldWithPath("content").description("내용"),
                                fieldWithPath("imageFileNames").description("이미지 파일 이름 리스트"),
                                fieldWithPath("thumbnail").description("썸네일 파일 이름")
                        )
                ));
    }

    @DisplayName("6. post 상세 조회")
    @Test
    void test_get() throws Exception {
        PostSaveUpdateRequest post = objectMapper
                .readValue(readJson("/json/post/create.json"), PostSaveUpdateRequest.class);

        String location = mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken())
                        .content(readJson("/json/post/create.json")))
                .andReturn()
                .getResponse().getHeader(HttpHeaders.LOCATION);

        Long postId = Long.parseLong(location.substring("/api/posts/".length()));

        List<String> filenames = postImageRepository.findByPostIdAndEnabled(postId, true)
                .stream().map(i -> i.getFilename())
                .collect(Collectors.toList());

        mockMvc.perform(get("/api/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andDo(resulthandler -> {
                    PostDetailResponse response = objectMapper.readValue(
                            resulthandler.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            PostDetailResponse.class);
                    PostTestHelper.assertPostResponse(response,
                            post.getTitle(),
                            post.getContent(),
                            post.getThumbnail(),
                            filenames,
                            tokenBox.getUserId(),
                            tokenBox.getUserNickName()
                    );
                })
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("postId").description("조회할 포스트 id")
                        ),
                        responseFields(
                                fieldWithPath("id").description("조회한 포스트 id"),
                                fieldWithPath("title").description("타이틀"),
                                fieldWithPath("content").description("내용"),
                                fieldWithPath("authorNickname").description("작성자 닉네임"),
                                fieldWithPath("authorId").description("작성자 id"),
                                fieldWithPath("images[]").description("이미지 리스트"),
                                fieldWithPath("images[].filename").description("이미지 파일 이름"),
                                fieldWithPath("images[].index").description("이미지 파일 노출 순서 1부터 시작"),
                                fieldWithPath("thumbnail").description("썸네일 파일 이름"),
                                fieldWithPath("enabled").description("포스트 활성화 여부"),
                                fieldWithPath("lastUpdatedAt").description("마지막으로 수정된 날짜")
                        )
                ));
    }

    @DisplayName("7. post 리스트 조회")
    @Test
    void test_list() throws Exception {
        PostSaveUpdateRequest post = objectMapper
                .readValue(readJson("/json/post/create.json"), PostSaveUpdateRequest.class);

        String location = mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken())
                        .content(readJson("/json/post/create.json")))
                .andReturn()
                .getResponse().getHeader(HttpHeaders.LOCATION);

        int size = 2;
        int page = 0;
        int totalpages = 1;
        mockMvc.perform(get("/api/posts")
                        .queryParam("size", "2")
                        .queryParam("page", "0")
                )
                .andExpect(status().isOk())
                .andDo(resulthandler -> {
                    PostListResponse response = objectMapper.readValue(
                            resulthandler.getResponse().getContentAsString(StandardCharsets.UTF_8),
                            PostListResponse.class);
                    assertEquals(page, response.getPageNo());
                    assertEquals(size, response.getPageSize());
                    assertEquals(totalpages, response.getTotalPages());
                })
                .andDo(restDocs.document(
                        requestParameters(
                                parameterWithName("size").description("페이지 사이즈 default 10"),
                                parameterWithName("page").description("페이지 번호 0부터 시작 default 0")
                        ),
                        responseFields(
                                fieldWithPath("pageNo").description("페이지 번호 0부터 시작"),
                                fieldWithPath("pageSize").description("페이지 사이즈"),
                                fieldWithPath("totalElements").description("총 포스트 개수"),
                                fieldWithPath("totalPages").description("총 페이지 수"),
                                fieldWithPath("last").description("마지막 페이지라면 true 마지막 페이지가 아니라면 false"),
                                fieldWithPath("posts").description("조회된 포스트 배열"),
                                fieldWithPath("posts[].postId").description("포스트 id"),
                                fieldWithPath("posts[].title").description("타이틀"),
                                fieldWithPath("posts[].content").description("내용"),
                                fieldWithPath("posts[].authorNickname").description("작성자 닉네임"),
                                fieldWithPath("posts[].authorId").description("작성자 id"),
                                fieldWithPath("posts[].thumbnail").description("썸네일 파일 이름"),
                                fieldWithPath("posts[].lastUpdatedAt").description("마지막으로 수정된 날짜")
                        )
                ));
    }

    @DisplayName("6. post 삭제")
    @Test
    void test_delete_post() throws Exception {
        PostSaveUpdateRequest requestDto = objectMapper.readValue(
                readJson("/json/post/create.json"),
                PostSaveUpdateRequest.class);
        Post post = postService.save(tokenBox.getUserId(), requestDto);

        mockMvc.perform(delete("/api/posts/{postId}", post.getId())
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken()))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME).description("유효한 jwt 인증 토큰")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("삭제할 포스트 id")
                        )
                ));
    }

}
