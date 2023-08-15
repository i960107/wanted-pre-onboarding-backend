package kr.co.wanted.posts.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndEnabled(Long id, boolean enabled);


    @Query("select u from User u join fetch u.authorities a where u.enabled=true and u.id = :userId")
    Optional<User> findByIdWithAuthorities(Long userId);

    @Query("select u from User u join fetch u.authorities a where u.enabled=true and u.email = :email")
    Optional<User> findByEmailWithAuthorities(String email);

    Optional<User> findByEmailAndEnabled(String email, boolean enabled);
}