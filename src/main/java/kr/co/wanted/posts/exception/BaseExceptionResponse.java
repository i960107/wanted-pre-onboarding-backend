package kr.co.wanted.posts.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BaseExceptionResponse {
    private int code;
    private String message;
}
