package kr.co.wanted.posts.web;

import kr.co.wanted.posts.domain.User;
import kr.co.wanted.posts.service.PostService;
import kr.co.wanted.posts.web.dto.PostSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    public static final String POST_RESOURCE_LOCATION = "/api/posts/";

    @PostMapping
    private ResponseEntity<Void> save(
            @AuthenticationPrincipal User user,
            @RequestBody PostSaveRequestDto requestDto) {
        Long postId = postService.save(user.getId(), requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, POST_RESOURCE_LOCATION + postId)
                .build();
    }
}
