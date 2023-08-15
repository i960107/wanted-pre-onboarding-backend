package kr.co.wanted.posts.web.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {
    @NotNull(message = "이메일은 반드시 @를 포함해야합니다.")
    @Email(message = "이메일은 반드시 @를 포함해야합니다.")
    private String email;

    @Min(value = 8, message = "비밀번호는 8자 이상이어야 합니다." )
    private String password;

    private String refreshToken;
}
