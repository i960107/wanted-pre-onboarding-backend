package kr.co.wanted.posts.web.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.co.wanted.posts.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserSaveRequest {
    private String name;
    @NotNull(message = "이메일은 반드시 @를 포함해야합니다.")
    @Email(message = "이메일은 반드시 @를 포함해야합니다.")
    private String email;
    private String nickName;
    @Size(min = 8, message = "패스워드는 8자이상이어야합니다.")
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
