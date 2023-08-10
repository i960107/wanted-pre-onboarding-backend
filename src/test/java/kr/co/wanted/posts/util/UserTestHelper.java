package kr.co.wanted.posts.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.service.UserService;

public class UserTestHelper {
    private UserService userService;

    public UserTestHelper(UserService userService) {
        this.userService = userService;
    }

    public User createUser(String name, List<String> authorities) throws BaseException {
        User user = userService.save(User.builder()
                .name(name)
                .email(name + "@email.com")
                .password(name + "1111")
                .build());
        for (String auth : authorities) {
            userService.addAuthority(user.getId(), auth);
        }
        return user;
    }

    public static void assertUser(User user, String name, String email, String nickName, String password,
                                  List<String> authorities) {
        assertNotNull(user.getId());
        assertEquals(user.getName(), name);
        assertNotNull(user.getEmail(), email);
        assertNotNull(user.getNickName(), nickName);
        assertNotNull(user.getPassword(), password);
        authorities.forEach(auth -> {
            assertTrue(user.hasAuthority(auth));
        });
    }

}
