package kr.co.wanted.posts.domain.post;

import java.util.Optional;
import kr.co.wanted.posts.web.dto.PostSimpleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByIdAndEnabled(Long postId, boolean enabled);

    @Query(value =
            "SELECT p from Post p join fetch p.images i "
                    + "where p.id = :postId "
    )
    Optional<Post> findByIdWithImages(Long postId);

    @Query(value =
            "SELECT new kr.co.wanted.posts.web.dto.PostSimpleResponse(p.id, p.title, p.content, p.thumbnail, u.id, u.nickName, p.updatedAt)"
                    + "from Post p inner join User u on p.author.id = u.id where p.enabled = true ",
            countQuery = "SELECT count(p) from Post p where p.enabled = true")
    Page<PostSimpleResponse> findEnabledPostsWithAuthor(Pageable pageable);

    @Query(value =
            "SELECT p from Post p left join fetch p.images i join fetch p.author u "
                    + "where p.enabled = true "
                    + "and (i.enabled is null or i.enabled = true)"
                    + "and p.id = :postId "
    )
    Optional<Post> findEnabledPostWithDetail(Long postId);
}