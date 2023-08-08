package kr.co.wanted.posts.domain.post;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import kr.co.wanted.posts.domain.BaseEntity;
import kr.co.wanted.posts.domain.user.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    private Long authorId;

    @Transient
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostImage> images = new ArrayList<>();

    private String thumbnailUrl;

    private boolean enabled;


    @Builder
    public Post(String title,
                Long authorId,
                User author,
                String content,
                List<String> imageUrls,
                String thumbnailUrl) {
        this.title = title;
        this.authorId = authorId;
        this.author = author;
        this.content = content;
        updateImages(imageUrls);
        this.thumbnailUrl = thumbnailUrl;
        this.enabled = true;
    }

    public void updateImages(List<String> imageUrls) {
        if (this.getImages().size() != 0) {
            this.images.forEach(PostImage::deleteImage);
        }

        IntStream.rangeClosed(1, imageUrls.size())
                .forEach(index -> {
                    this.images.add(PostImage.builder()
                            .post(this)
                            .index(index)
                            .imageUrl(imageUrls.get(index - 1))
                            .build());
                });
    }

}
