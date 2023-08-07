package kr.co.wanted.posts.domain.post;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import kr.co.wanted.posts.util.PostTestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
class PostRepositoryTest {
    @Autowired
    PostRepository postRepository;

    @Autowired
    PostImageRepository postImageRepository;

    @DisplayName("게시글 등록에 성공한다.")
    @Test
    void test_save() {
        //given
        String title = "title";
        String content = "content";
        String thumbnailUrl = "thumbnailUrl";
        List<String> imageUrls = List.of("imageUrl1", "imageUrl2");
        Post givenPost = Post.builder()
                .title(title)
                .content(content)
                .thumbnailUrl(thumbnailUrl)
                .imageUrls(imageUrls)
                .build();

        //when
        Post post = postRepository.save(givenPost);

        assertEquals(1, postRepository.count());
        assertEquals(1L, post.getId());
        PostTestHelper.assertPost(post, title, content, thumbnailUrl, imageUrls);
    }
}