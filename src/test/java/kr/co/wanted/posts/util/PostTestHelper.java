package kr.co.wanted.posts.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import kr.co.wanted.posts.domain.post.Post;
import kr.co.wanted.posts.domain.post.PostImage;

public class PostTestHelper {

    public static void assertPost(Post post, String title, String content,
                                  String thumbnailUrl, List<String> imageUrls) {
        assertNotNull(post.getId());
        assertTrue(post.getCreatedAt().isBefore(LocalDateTime.now()));
        assertTrue(post.getUpdatedAt().isBefore(LocalDateTime.now()));
        assertTrue(post.isEnabled());

        assertEquals(post.getTitle(), title);
        assertEquals(post.getContent(), content);
        assertEquals(post.getThumbnailUrl(), thumbnailUrl);

        assertPostImage(post.getImages(), imageUrls);

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
                    assertEquals(image.getImageUrl(), imageUrls.get(index - 1));
                });
    }
}
