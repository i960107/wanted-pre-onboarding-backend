package kr.co.wanted.posts.domain.post;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import kr.co.wanted.posts.domain.BaseEntity;
import kr.co.wanted.posts.domain.user.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode(callSuper = false, of = {"id"})
@NoArgsConstructor
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @OrderBy(value = "index ASC")
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostImage> images = new ArrayList<>();

    private String thumbnail;

    private boolean enabled;


    @Builder
    public Post(
            Long postId,
            User author,
            String title,
            String content,
            List<String> imageFileNames,
            String thumbnail) {
        this.id = postId;
        this.title = title;
        this.author = author;
        this.content = content;
        createOrChangeImages(imageFileNames);
        this.thumbnail = thumbnail;
        this.enabled = true;
    }

    public void createOrChangeImages(List<String> imageFileNames) {
        if (this.getImages().size() != 0) {
            this.images.forEach(PostImage::delete);
        }

        IntStream.rangeClosed(1, images.size())
                .forEach(index -> {
                    this.images.add(PostImage.builder()
                            .post(this)
                            .index(index)
                            .filename(imageFileNames.get(index - 1))
                            .build());
                });
    }

    public void delete() {
        this.enabled = false;
        deleteImages();
    }

    public void deleteImages() {
        this.images.forEach(image -> image.delete());
    }
}
