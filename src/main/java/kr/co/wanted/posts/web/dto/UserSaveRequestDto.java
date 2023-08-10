package kr.co.wanted.posts.web.dto;

import kr.co.wanted.posts.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserSaveRequestDto {
    private String name;
    private String email;
    private String nickName;
    private String password;

    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .nickName(nickName)
                .password(password)
                .build();
    }
}
