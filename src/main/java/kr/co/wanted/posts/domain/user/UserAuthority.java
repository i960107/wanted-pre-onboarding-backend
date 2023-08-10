package kr.co.wanted.posts.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import kr.co.wanted.posts.domain.BaseEntity;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Entity
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
public class UserAuthority extends BaseEntity implements GrantedAuthority {
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    @EmbeddedId
    private UserAuthorityId id;


    @JsonIgnore
    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public UserAuthority(UserAuthorityId id, User user) {
        this.id = id;
        this.user = user;
    }

    @Override
    public String getAuthority() {
        return id.getAuthority();
    }

    @Override
    public String toString() {
        return "UserAuthority{" +
                "id=" + id +
                ", user=" + user.getId() +
                '}';
    }
}
