package kr.co.wanted.posts.domain.post;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

    private String filename;

    private int index;

    private boolean enabled;

    @Builder
    public PostImage(Post post, String filename, int index) {
        this.post = post;
        this.filename = filename;
        this.index = index;
        this.enabled = true;
    }

    public void delete() {
        this.enabled = false;
    }
}
