package kr.co.wanted.posts.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BaseException extends Exception {
    private int code;
    private String message;

    public BaseException(BaseExceptions exception) {
        this.code = exception.getCode();
        this.message = exception.getMessage();
    }

    @Getter
    public enum BaseExceptions {
        POST_NOT_FOUND(4001, "등록되지 않았거나 삭제된 게시물입니다."),
        USER_EMAIL_NOT_FOUND(4002, "잘못된 이메일 입니다."),
        USER__NOT_FOUND(4003, "존재하지 않는 유저입니다.");

        private int code;
        private String message;

        BaseExceptions(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
