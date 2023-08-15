package kr.co.wanted.posts.web.dto;

import kr.co.wanted.posts.domain.post.PostImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImageResponse {
    private int index;
    private String filename;

    public static PostImageResponse from(PostImage postImage) {
        return PostImageResponse.builder()
                .index(postImage.getIndex())
                .filename(postImage.getFilename())
                .build();
    }
}
