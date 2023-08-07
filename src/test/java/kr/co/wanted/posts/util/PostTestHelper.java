package kr.co.wanted.posts.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import kr.co.wanted.posts.domain.post.Post;

public class PostTestHelper {

    public static void assertPost(Post post, String title, String content,
                                  String thumbnailUrl, List<String> imageUrls) {
        assertNotNull(post.getId());
        assertTrue(post.getCreatedAt().isBefore(LocalDateTime.now()));
        assertTrue(post.getUpdatedAt().isBefore(LocalDateTime.now()));
        assertFalse(post.isDeleted());

        assertEquals(post.getTitle(), title);
        assertEquals(post.getContent(), content);
        assertEquals(post.getThumbnailUrl(), thumbnailUrl);

        assertEquals(post.getImageUrls().size(), imageUrls.size());
    }
}
