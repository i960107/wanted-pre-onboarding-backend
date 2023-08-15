package kr.co.wanted.posts.web;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kr.co.wanted.posts.config.jwt.JwtLoginFilter;
import kr.co.wanted.posts.domain.post.Post;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.util.PostTestHelper;
import kr.co.wanted.posts.util.TestHelper;
import kr.co.wanted.posts.util.TokenBox;
import kr.co.wanted.posts.web.dto.PostDetailResponse;
import kr.co.wanted.posts.web.dto.PostListResponse;
import kr.co.wanted.posts.web.dto.PostSaveUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class PostTest extends TestHelper {
    @DisplayName("게시글을 생성한다.")
    @Test
    void test_create_post() throws Exception {
        TokenBox tokenBox = createUserAndGetAuthToken();

        PostSaveUpdateRequest requestDto = objectMapper
                .readValue(readJson("/json/post/create.json"), PostSaveUpdateRequest.class);

        URI uri = uri("/api/posts");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken());

        HttpEntity<PostSaveUpdateRequest> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<Void> response = testRestTemplate.exchange(uri, HttpMethod.POST,
                request, Void.class);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.LOCATION));

        ResponseEntity<PostDetailResponse> redirectedResponse = testRestTemplate.exchange(
                response.getHeaders().getLocation(),
                HttpMethod.GET,
                new HttpEntity<>(null), PostDetailResponse.class);

        PostTestHelper.assertPostResponse(
                redirectedResponse.getBody(),
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getThumbnail(),
                requestDto.getImageFileNames(),
                tokenBox.getUserId(),
                tokenBox.getUserNickName()
        );
    }

    @DisplayName("이미지가 없이 게시글을 생성한다.")
    @Test
    void test_create_post_without_images() throws Exception {
        TokenBox tokenBox = createUserAndGetAuthToken();

        PostSaveUpdateRequest requestDto = objectMapper
                .readValue(readJson("/json/post/create-without-images.json"), PostSaveUpdateRequest.class);

        URI uri = uri("/api/posts");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken());

        HttpEntity<PostSaveUpdateRequest> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<Void> response = testRestTemplate.exchange(uri, HttpMethod.POST,
                request, Void.class);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.LOCATION));

        ResponseEntity<PostDetailResponse> redirectedResponse = testRestTemplate.exchange(
                response.getHeaders().getLocation(),
                HttpMethod.GET,
                new HttpEntity<>(null), PostDetailResponse.class);

        List<String> imageFilenames =
                requestDto.getImageFileNames() != null ? requestDto.getImageFileNames() : new ArrayList<>();
        PostTestHelper.assertPostResponse(
                redirectedResponse.getBody(),
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getThumbnail(),
                imageFilenames,
                tokenBox.getUserId(),
                tokenBox.getUserNickName()
        );
    }

    @DisplayName("토큰이 없으면 게시글을 생성할 수 없다.")
    @Test
    void test_create_post_fail() throws Exception {
        TokenBox tokenBox = createUserAndGetAuthToken();

        PostSaveUpdateRequest requestDto = objectMapper
                .readValue(readJson("/json/post/create.json"), PostSaveUpdateRequest.class);

        URI uri = uri("/api/posts");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<PostSaveUpdateRequest> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<Void> response = testRestTemplate.exchange(uri, HttpMethod.POST,
                request, Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @DisplayName("자신의 글을 수정한다.")
    @Test
    void test_update_own_post() throws Exception {
        TokenBox tokenBox = createUserAndGetAuthToken();
        Post post = createPost(tokenBox.getUserId());

        URI uri = uri("/api/posts/" + post.getId());
        PostSaveUpdateRequest updateRequestDto = objectMapper
                .readValue(readJson("/json/post/update.json"), PostSaveUpdateRequest.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken());
        HttpEntity<PostSaveUpdateRequest> updateRequest = new HttpEntity<>(updateRequestDto, headers);
        ResponseEntity<Void> response = testRestTemplate.exchange(uri, HttpMethod.PUT,
                updateRequest, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        PostDetailResponse updatedPost = postService.findEnabledPostWithDetail(post.getId());
        PostTestHelper.assertPostResponse(
                updatedPost,
                updateRequestDto.getTitle(),
                updateRequestDto.getContent(),
                updateRequestDto.getThumbnail(),
                updateRequestDto.getImageFileNames(),
                tokenBox.getUserId(),
                tokenBox.getUserNickName()
        );
    }

    @DisplayName("남의 글을 수정할 수 없다.")
    @Test
    void test_fail_on_update_others_post() throws Exception {
        TokenBox tokenBox = createUserAndGetAuthToken();
        User other = createUser("other");
        Post post = createPost(other.getId());

        URI uri = uri("/api/posts/" + post.getId());
        PostSaveUpdateRequest updateRequestDto = objectMapper
                .readValue(readJson("/json/post/update.json"), PostSaveUpdateRequest.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken());
        HttpEntity<PostSaveUpdateRequest> updateRequest = new HttpEntity<>(updateRequestDto, headers);
        ResponseEntity<Void> response = testRestTemplate.exchange(uri, HttpMethod.PUT,
                updateRequest, Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @DisplayName("남의 글을 삭제할 수 없다.")
    @Test
    void test_fail_on_delete_others_post() throws Exception {
        TokenBox tokenBox = createUserAndGetAuthToken();
        User other = createUser("other");
        Post post = createPost(other.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken());
        ResponseEntity<Void> response = testRestTemplate.exchange(
                uri("/api/posts/" + post.getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @DisplayName("자신의 글을 삭제한다.")
    @Test
    void test_delete_own_post() throws Exception {
        TokenBox tokenBox = createUserAndGetAuthToken();
        Post post = createPost(tokenBox.getUserId());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME, tokenBox.getAuthToken());
        ResponseEntity<Void> response = testRestTemplate.exchange(
                uri("/api/posts/" + post.getId()),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Post deletedPost = postRepository.findByIdWithImages(post.getId()).get();

        assertNotNull(deletedPost.getId());
        assertFalse(deletedPost.isEnabled());
        deletedPost.getImages().forEach(postImage -> {
            assertFalse(postImage.isEnabled());
        });
    }

    @DisplayName("글을 상세 조회한다.")
    @Test
    void test_get() throws Exception {
        TokenBox tokenBox = createUserAndGetAuthToken();
        Post post = createPost(tokenBox.getUserId());
        ResponseEntity<PostDetailResponse> response = testRestTemplate.exchange(
                uri("/api/posts/" + post.getId()),
                HttpMethod.GET,
                new HttpEntity<>(null), PostDetailResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<String> filenames = postImageRepository.findByPostIdAndEnabled(post.getId(), true)
                .stream().map(i -> i.getFilename())
                .collect(Collectors.toList());
        PostTestHelper.assertPostResponse(response.getBody(),
                post.getTitle(),
                post.getContent(),
                post.getThumbnail(),
                filenames,
                tokenBox.getUserId(), tokenBox.getUserNickName());
    }

    @DisplayName("글을 리스트 조회한다.")
    @Test
    void test_list() throws Exception {
        TokenBox tokenBox = createUserAndGetAuthToken();
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            posts.add(createPost(tokenBox.getUserId()));
        }

        int size = 2;
        int page = 0;
        int totalpages = (posts.size() + 1) / size;

        ResponseEntity<PostListResponse> response = testRestTemplate
                .getForEntity(uri("/api/posts?size=" + size + "&page=" + page), PostListResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PostListResponse list = response.getBody();
        assertEquals(page, list.getPageNo());
        assertEquals(size, list.getPageSize());
        assertEquals(totalpages, list.getTotalPages());
        assertThat(list.getPosts().stream().map(post -> post.getPostId()).collect(Collectors.toList()))
                .containsExactly(
                        posts.get(posts.size() - 1).getId(),
                        posts.get(posts.size() - 2).getId()
                );
    }
}
