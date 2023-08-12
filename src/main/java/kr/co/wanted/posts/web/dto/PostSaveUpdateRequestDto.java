package kr.co.wanted.posts.web.dto;

import java.util.List;
import kr.co.wanted.posts.domain.post.Post;
import kr.co.wanted.posts.domain.user.User;
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

    public Post toEntity(User author) {
        return Post.builder()
                .author(author)
                .title(title)
                .content(content)
                .imageUrls(imageUrls)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }


}
