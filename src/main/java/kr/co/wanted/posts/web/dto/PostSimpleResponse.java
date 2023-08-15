package kr.co.wanted.posts.web.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostSimpleResponse {
    private Long postId;
    private String title;
    private String content;
    private String thumbnail;
    private Long authorId;
    private String authorNickname;
    private LocalDateTime lastUpdatedAt;
}
