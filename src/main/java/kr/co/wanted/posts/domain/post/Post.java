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
import kr.co.wanted.posts.domain.BaseEntity;
import kr.co.wanted.posts.domain.User;
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
    private List<PostImage> imageUrls = new ArrayList<>();

    private String thumbnailUrl;

    private boolean isDeleted;


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
        imageUrls.forEach(imageUrl -> {
            this.imageUrls.add(PostImage.builder()
                    .post(this)
                    .imageUrl(imageUrl)
                    .build());
        });
        this.thumbnailUrl = thumbnailUrl;
        this.isDeleted = false;
    }
}
