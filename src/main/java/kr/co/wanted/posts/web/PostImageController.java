package kr.co.wanted.posts.web;

import kr.co.wanted.posts.service.PostImageService;
import kr.co.wanted.posts.web.dto.PresignedKeyDto;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class PostImageController {
    private final PostImageService postImageService;

    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public PresignedKeyDto getPresigendKey(@RequestParam String fileName) {
        return postImageService.getPresignedUrl(fileName);
    }

}
