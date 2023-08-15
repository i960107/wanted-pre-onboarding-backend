package kr.co.wanted.posts.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import kr.co.wanted.posts.domain.post.Post;
import kr.co.wanted.posts.domain.post.PostImage;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.service.PostService;
import kr.co.wanted.posts.web.dto.PostDetailResponse;
import kr.co.wanted.posts.web.dto.PostImageResponse;
import kr.co.wanted.posts.web.dto.PostSaveUpdateRequest;

public class PostTestHelper {
    protected PostService postService;

    public PostTestHelper(PostService postService) {
        this.postService = postService;
    }

    public Post createPost(User user,
                           String title,
                           String content,
                           String thumbnailUrl,
                           List<String> imageUrls
    ) throws BaseException {
        return postService.save(user.getId(), new PostSaveUpdateRequest(title, content, imageUrls, thumbnailUrl));
    }

    public static void assertPost(Post post, String title, String content,
                                  String thumbnailUrl, List<String> imageUrls) {
        assertNotNull(post.getId());
        assertTrue(post.getCreatedAt().isBefore(LocalDateTime.now()));
        assertTrue(post.getUpdatedAt().isBefore(LocalDateTime.now()));
        assertTrue(post.isEnabled());

        assertEquals(post.getTitle(), title);
        assertEquals(post.getContent(), content);
        assertEquals(post.getThumbnail(), thumbnailUrl);

        assertPostImage(post.getImages(), imageUrls);
    }


    public static void assertPostResponse(PostDetailResponse post, String title, String content,
                                          String thumbnailUrl, List<String> imageUrls,
                                          Long authorId, String authorNickName
    ) {
        assertNotNull(post.getId());
        assertTrue(post.getLastUpdatedAt().isBefore(LocalDateTime.now()));
        assertTrue(post.isEnabled());

        assertEquals(post.getTitle(), title);
        assertEquals(post.getContent(), content);
        assertEquals(post.getThumbnail(), thumbnailUrl);
        assertEquals(post.getAuthorId(), authorId);
        assertEquals(post.getAuthorNickname(), authorNickName);
        assertPostImageResponse(post.getImages(), imageUrls);

    }

    public static void assertPostImage(List<PostImage> images, List<String> imageUrls) {
        assertEquals(images.size(), imageUrls.size());
        IntStream.rangeClosed(1, images.size())
                .forEach(index -> {
                    PostImage image = images.get(index - 1);
                    assertNotNull(image.getId());
                    assertNotNull(image.getCreatedAt());
                    assertNotNull(image.getUpdatedAt());
                    assertTrue(image.isEnabled());
                    assertEquals(image.getIndex(), index);
                    assertEquals(image.getFilename(), imageUrls.get(index - 1));
                });
    }

    public static void assertPostImageResponse(List<PostImageResponse> images, List<String> fileNames) {
        assertEquals(images.size(), fileNames.size());
        IntStream.rangeClosed(1, images.size())
                .forEach(index -> {
                    PostImageResponse image = images.get(index - 1);
                    assertNotNull(image.getIndex());
                    assertEquals(image.getFilename(), fileNames.get(index - 1));
                });
    }
}
