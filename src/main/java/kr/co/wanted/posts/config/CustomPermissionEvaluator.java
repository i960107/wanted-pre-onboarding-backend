package kr.co.wanted.posts.config;

import java.io.Serializable;
import kr.co.wanted.posts.domain.post.Post;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.domain.user.UserAuthority;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {
    private final PostService postService;

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
                                 Object permission) {
        if (!targetType.equalsIgnoreCase("POST")) {
            return false;
        }

        Post post;
        try {
            post = postService.findById((Long) targetId);
        } catch (BaseException exception) {
            throw new AccessDeniedException("존재하지 않는 게시물입니다.");
        }

        User user = (User) authentication.getPrincipal();
        if (user.hasAuthority(UserAuthority.ROLE_USER)
                && post.getAuthor().getId().equals(user.getId())) {
            return true;
        }

        return false;
    }


}
