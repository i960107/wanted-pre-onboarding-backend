package kr.co.wanted.posts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import kr.co.wanted.posts.config.JpaConfig;
import kr.co.wanted.posts.domain.post.Post;
import kr.co.wanted.posts.domain.post.PostImageRepository;
import kr.co.wanted.posts.domain.post.PostRepository;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.domain.user.UserAuthorityRepository;
import kr.co.wanted.posts.domain.user.UserRepository;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.util.PostTestHelper;
import kr.co.wanted.posts.util.UserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@DataJpaTest
@Import(JpaConfig.class)
class PostTest {
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostImageRepository postImageRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserAuthorityRepository userAuthorityRepository;

    PostService postService;
    UserService userService;

    PostTestHelper postTestHelper;
    UserTestHelper userTestHelper;

    User user;
    String userName;

    Post post;
    String title;
    String content;
    String thumbnailUrl;
    List<String> imageUrls;


    @BeforeEach
    void init() throws BaseException {
        userService = new UserService(userRepository, userAuthorityRepository, new BCryptPasswordEncoder());
        postService = new PostService(postRepository, userService);
        postTestHelper = new PostTestHelper(postService);
        userTestHelper = new UserTestHelper(userService);

        userName = "user";
        user = userTestHelper.createUser(userName, List.of("USER"));

        title = "title";
        content = "content";
        thumbnailUrl = "thumbnailUrl";
        imageUrls = List.of("imageUrl1", "imageUrl2");

        post = postTestHelper.createPost(
                user,
                title,
                content,
                thumbnailUrl,
                imageUrls
        );
    }

    @DisplayName("게시글 등록에 성공한다.")
    @Test
    void test_save() {
        //given
        String title = "test1-title";
        String content = "test1-content";
        String thumbnailUrl = "thumbnailUrl";
        List<String> imageUrls = List.of("test1-imageUrl1", "test1-imageUrl2");
        Post givenPost = Post.builder()
                .title(title)
                .content(content)
                .thumbnailUrl(thumbnailUrl)
                .imageUrls(imageUrls)
                .build();

        //when
        Post post = postRepository.save(givenPost);

        //then
        assertEquals(2, postRepository.count());
        assertEquals(4, postImageRepository.count());
        PostTestHelper.assertPost(post, title, content, thumbnailUrl, imageUrls);
    }

    @DisplayName("존재하는 게시글을 조회하면 정렬된 이미지와 글쓴이까지 조회된다.")
    @Test
    void test_get() {
        //when
        Optional<Post> optionalPost = postRepository.findByIdAndEnabled(post.getId(), true);

        //then
        assertTrue(optionalPost.isPresent());
        assertEquals(optionalPost.get().getImages().get(0).getIndex(), 1);
        assertEquals(optionalPost.get().getImages().get(0).getIndex(), 2);
        assertThat(optionalPost.get())
                .isNotNull();
        PostTestHelper.assertPost(post, title, content, thumbnailUrl, imageUrls);
    }

    @DisplayName("존재하지 않거나 삭제된 게시물을 조회한다.")
    @Test
    void test_get_fail() {
    }
}