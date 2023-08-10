package kr.co.wanted.posts.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import kr.co.wanted.posts.config.JpaConfig;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.domain.user.UserAuthorityRepository;
import kr.co.wanted.posts.domain.user.UserRepository;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.util.UserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@Import(JpaConfig.class)
public class UserTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAuthorityRepository userAuthorityRepository;
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    private UserTestHelper userTestHelper;

    User user;
    String userName;
    List<String> authorities;

    @BeforeEach
    void init() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, userAuthorityRepository, passwordEncoder);
        userTestHelper = new UserTestHelper(userService);
    }

    private void createUser() throws BaseException {
        userName = "user";
        authorities = List.of("USER");
        user = userTestHelper.createUser(userName, authorities);
    }

    @DisplayName("1. 권한이 없는 유저를 생성한다.")
    @Test
    void test_save() {
        String name = "test-user";
        String password = passwordEncoder.encode("test-user1111");
        String email = "test-user@email.com";
        String nickName = "micky";
        User user = userService.save(User.builder()
                .name(name)
                .password(password)
                .nickName(nickName)
                .email(email)
                .build());
        UserTestHelper.assertUser(user, name, email, nickName, password, List.of());
        assertEquals(0, user.getAuthorities().size());
    }

    @DisplayName("2. 유저에게 권한을 준다.")
    @Test
    void test_save_and_add_authority() throws BaseException {
        //when
        createUser();

        //then
        assertTrue(userService.findEnabledUserById(user.getId()).hasAuthority("USER"));
        user.getUserAuthorities().stream().forEach(auth -> {
            assertEquals(auth.getUser(), user);
        });
        assertEquals(1, userAuthorityRepository.count());

    }
}

