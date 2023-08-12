package kr.co.wanted.posts.web;

import javax.validation.Valid;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.service.UserService;
import kr.co.wanted.posts.web.dto.UserSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserSaveRequestDto requestDto) throws BaseException {
        Long userId = userService.save(requestDto.toEntity()).getId();
        return ResponseEntity
                .status(HttpStatus.TEMPORARY_REDIRECT)
                .header(HttpHeaders.LOCATION, "/")
                .build();
    }
}
