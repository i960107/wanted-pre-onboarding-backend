package kr.co.wanted.posts.domain.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kr.co.wanted.posts.domain.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class PostImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_image_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private String imageUrl;

    private int index;

    private boolean enabled;

    @Builder
    public PostImage(Post post, String imageUrl, int index) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.index = index;
        this.enabled = true;
    }

    public void deleteImage(){
        this.enabled = false;
    }
}
