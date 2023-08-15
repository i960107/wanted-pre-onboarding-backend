package kr.co.wanted.posts.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import kr.co.wanted.posts.domain.post.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String authorNickname;
    private Long authorId;
    private List<PostImageResponse> images;
    private String thumbnail;
    private boolean enabled;
    private LocalDateTime lastUpdatedAt;

    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .authorId(post.getAuthor().getId())
                .authorNickname(post.getAuthor().getNickName())
                .content(post.getContent())
                .images(post.getImages()
                        .stream()
                        .map(PostImageResponse::from)
                        .collect(Collectors.toList()))
                .thumbnail(post.getThumbnail())
                .enabled(post.isEnabled())
                .lastUpdatedAt(post.getUpdatedAt())
                .build();
    }

}
