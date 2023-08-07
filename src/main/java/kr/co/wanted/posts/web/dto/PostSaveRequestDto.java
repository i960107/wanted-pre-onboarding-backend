package kr.co.wanted.posts.web.dto;

import java.util.List;
import kr.co.wanted.posts.domain.post.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSaveRequestDto {
    private String title;
    private String content;
    private List<String> imageUrls;
    private String thumbnailUrl;

    public Post toEntity(Long authorId) {
        return Post.builder()
                .authorId(authorId)
                .title(title)
                .content(content)
                .imageUrls(imageUrls)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }


}
