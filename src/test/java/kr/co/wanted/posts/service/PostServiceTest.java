package kr.co.wanted.posts.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import kr.co.wanted.posts.domain.post.PostRepository;
import kr.co.wanted.posts.web.dto.PostSaveRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@Transactional
class PostServiceTest {
    @MockBean
    private PostRepository postRepository;

    private PostService postService;

    @BeforeEach
    void before() {
        postService = new PostService(postRepository);
    }


}