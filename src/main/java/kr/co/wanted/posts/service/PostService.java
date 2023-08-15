package kr.co.wanted.posts.service;

import java.util.Optional;
import kr.co.wanted.posts.domain.post.Post;
import kr.co.wanted.posts.domain.post.PostImageRepository;
import kr.co.wanted.posts.domain.post.PostRepository;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.exception.BaseException.BaseExceptions;
import kr.co.wanted.posts.web.dto.PostDetailResponse;
import kr.co.wanted.posts.web.dto.PostListResponse;
import kr.co.wanted.posts.web.dto.PostSaveUpdateRequest;
import kr.co.wanted.posts.web.dto.PostSimpleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final PostImageRepository postImageRepository;

    public Post save(Long authorId, PostSaveUpdateRequest requestDto) throws BaseException {
        User author = userService.getUserReference(authorId);
        return postRepository
                .save(requestDto.toEntity(author));
    }

    public void update(Long postId, Long userId, PostSaveUpdateRequest requestDto) throws BaseException {
        Post originalPost = postRepository.findEnabledPostWithDetail(postId)
                .orElseThrow(() -> new BaseException(BaseExceptions.POST_NOT_FOUND));
        originalPost.deleteImages();

        User user = userService.getUserReference(userId);
        Post newPost = requestDto.toEntity(postId, user);
        postRepository.save(newPost);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse findEnabledPostWithDetail(Long postId) throws BaseException {
        Post post = postRepository.findEnabledPostWithDetail(postId)
                .orElseThrow(() -> new BaseException(BaseExceptions.POST_NOT_FOUND));
        return PostDetailResponse.from(post);
    }

    @Transactional(readOnly = true)
    public Post findEnabledPostById(Long postId) throws BaseException {
        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty() || !post.get().isEnabled()) {
            throw new BaseException(BaseExceptions.POST_NOT_FOUND);
        }
        return post.get();
    }

    @Transactional(readOnly = true)
    public Post findById(Long postId) throws BaseException {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(BaseExceptions.POST_NOT_FOUND));
    }

    public void deleteById(Long postId) throws BaseException {
        postRepository.findEnabledPostWithDetail(postId)
                .ifPresent(post -> {
                    post.delete();
                });
    }

    @Transactional(readOnly = true)
    public PostListResponse list(Pageable pageable) {
        Page<PostSimpleResponse> pagePost = postRepository.findEnabledPostsWithAuthor(pageable);

        return PostListResponse
                .builder()
                .pageNo(pagePost.getNumber())
                .pageSize(pagePost.getSize())
                .totalElements(pagePost.getTotalElements())
                .totalPages(pagePost.getTotalPages())
                .last(pagePost.isLast())
                .posts(pagePost.getContent())
                .build();
    }
}
