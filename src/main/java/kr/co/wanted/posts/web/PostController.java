package kr.co.wanted.posts.web;

import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.service.PostService;
import kr.co.wanted.posts.web.dto.PostDetailResponse;
import kr.co.wanted.posts.web.dto.PostListResponse;
import kr.co.wanted.posts.web.dto.PostSaveUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private static final String POST_RESOURCE_LOCATION = "/api/posts/";
    private final PostService postService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> save(
            @AuthenticationPrincipal User user,
            @RequestBody PostSaveUpdateRequest requestDto) throws BaseException {
        if (user == null) {
            throw new AccessDeniedException("유저만 게시물 등록이 가능합니다.");
        }
        Long postId = postService.save(user.getId(), requestDto).getId();

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, POST_RESOURCE_LOCATION + postId)
                .build();
    }

    @GetMapping
    public PostListResponse list(
            @PageableDefault(size = 10, page = 0, sort = {"createdAt"}, direction = Direction.DESC) Pageable pageable) {
        return postService.list(pageable);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> get(@PathVariable Long postId) throws BaseException {
        return ResponseEntity.ok(postService.findEnabledPostWithDetail(postId));
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasPermission(#postId, 'POST', 'UPDATE')")
    public void update(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @RequestBody PostSaveUpdateRequest requestDto
    ) throws BaseException {
        postService.update(postId, user.getId(), requestDto);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasPermission(#postId, 'POST', 'DELETE')")
    public void delete(
            @PathVariable Long postId
    ) throws BaseException {
        postService.deleteById(postId);
    }


}
