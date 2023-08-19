package kr.co.wanted.posts.web.dto;

import java.util.Map;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.exception.BaseException.BaseResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getterr
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T result;

    public static BaseResponse of(BaseException exception) {
        return BaseResponse.builder()
                .success(false)
                .code(exception.getCode())
                .message(exception.getMessage())
                .build();
    }

    public static BaseResponse of(BaseException exception, Map<String, String> bindingResult) {
        return BaseResponse.builder()
                .success(false)
                .code(exception.getCode())
                .message(exception.getMessage())
                .result(bindingResult)
                .build();
    }

    public static BaseResponse of(Object result) {
        return BaseResponse.builder()
                .success(true)
                .code(BaseResponseStatus.SUCCESS.getCode())
                .message(BaseResponseStatus.SUCCESS.getMessage())
                .result(result)
                .build();
    }
}
