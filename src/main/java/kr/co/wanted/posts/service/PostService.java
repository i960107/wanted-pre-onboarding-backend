package kr.co.wanted.posts.service;

import kr.co.wanted.posts.domain.post.PostRepository;
import kr.co.wanted.posts.web.dto.PostSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public Long save(Long authorId, PostSaveRequestDto requestDto) {
        return postRepository
                .save(requestDto.toEntity(authorId))
                .getId();
    }

}
