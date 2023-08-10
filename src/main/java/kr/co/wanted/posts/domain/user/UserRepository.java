package kr.co.wanted.posts.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndEnabled(Long id, boolean enabled);

    Optional<User> findByEmail(String username);

    @Query("select u from User u join fetch UserAuthority a on u.id = a.id.userId where u.enabled=true")
    Optional<User> findByIdWithAuthorities(Long userId);
}