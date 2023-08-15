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
public class PostSaveUpdateRequest {
    private String title;
    private String content;
    private List<String> imageFileNames;
    private String thumbnail;

    public Post toEntity(User author) {
        return Post.builder()
                .author(author)
                .title(title)
                .content(content)
                .thumbnail(thumbnail)
                .imageFileNames(imageFileNames)
                .build();
    }

    public Post toEntity(Long postId, User author) {
        return Post.builder()
                .postId(postId)
                .author(author)
                .title(title)
                .content(content)
                .imageFileNames(imageFileNames)
                .thumbnail(thumbnail)
                .build();
    }


}
