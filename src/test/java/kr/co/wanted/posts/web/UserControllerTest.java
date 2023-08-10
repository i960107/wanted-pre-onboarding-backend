package kr.co.wanted.posts.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import kr.co.wanted.posts.domain.user.UserAuthority;
import kr.co.wanted.posts.service.UserService;
import kr.co.wanted.posts.util.TestHelper;
import kr.co.wanted.posts.util.TokenBox;
import kr.co.wanted.posts.util.UserTestHelper;
import kr.co.wanted.posts.web.dto.UserLoginDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

public class UserControllerTest extends TestHelper {
    @Autowired
    private UserService userService;

    private TokenBox getAuthToken() throws Exception {
        UserTestHelper userTestHelper = new UserTestHelper(userService);

        userTestHelper.createUser("user", List.of(UserAuthority.ROLE_USER));

        UserLoginDto userLoginDto = new UserLoginDto("user@email.com", "user1111", null);
        HttpServletResponse response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDto)))
                .andReturn()
                .getResponse();
        return new TokenBox(response.getHeader("auth_token"), response.getHeader("refresh_token"));
    }


    @DisplayName("1.로그인을 하면 token을 두개 받는다.")
    @Test
    void test_login() throws Exception {
        //given
        UserTestHelper userTestHelper = new UserTestHelper(userService);
        userTestHelper.createUser("user", List.of(UserAuthority.ROLE_USER));

        UserLoginDto userLoginDto = new UserLoginDto("user@email.com", "user1111", null);

        //when
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDto)))
                .andExpect(header().exists("auth_token"))
                .andExpect(header().exists("refresh_token"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }


    @DisplayName("2. 만료된 토큰을 사용한다.")
    @Test
    void test_refresh_token() throws Exception {
        TokenBox tokenBox = getAuthToken();

        Thread.sleep(3000);

        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("auth_token", tokenBox.getAuthToken()))
                .andExpect(status().is(401))
                .andExpect(content().string("token is not valid"));
    }

    @DisplayName("3. 만료된 토큰을 갱신한다.")
    @Test
    void test_refresh_auth_token() throws Exception {
        TokenBox tokenBox = getAuthToken();

        Thread.sleep(3000);

        UserLoginDto loginDto = new UserLoginDto(null, null, tokenBox.getRefreshToken());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(header().exists("auth_token"))
                .andExpect(header().exists("refresh_token"))
                .andExpect(status().is3xxRedirection());
    }

}