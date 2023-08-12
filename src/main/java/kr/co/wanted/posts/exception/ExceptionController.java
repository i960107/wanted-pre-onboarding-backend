package kr.co.wanted.posts.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseException> baseException(BaseException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validationException(MethodArgumentNotValidException exception) {
        Map<String, String> message = new HashMap<>();
        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(fieldError -> message.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(message);
    }

}
