package kr.co.wanted.posts.config.jwt;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.exception.BaseException.BaseExceptions;
import kr.co.wanted.posts.service.UserService;
import kr.co.wanted.posts.web.dto.UserLoginDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {
    public static final String AUTH_TOKEN_HEADER_NAME = "auth_token";
    public static final String REFRESH_TOKEN_HEADER_NAME = "refresh_token";

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final UserService userService;


    public JwtLoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        super(authenticationManager);
        objectMapper = new ObjectMapper();
        setFilterProcessesUrl("/login");
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        UserLoginDto loginDto = null;
        try {
            loginDto = obtainLoginDto(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (loginDto.getRefreshToken() != null) {
            return attemptAuthenticationWithRefreshToken(loginDto.getRefreshToken());
        }

        String username = loginDto.getEmail();
        username = (username != null) ? username : "";
        username = username.trim();
        if (!username.contains("@")) {
            throw new RuntimeException(BaseExceptions.INVALID_EMAIL.getMessage());
        }

        String password = loginDto.getPassword();
        password = (password != null) ? password : "";
        if (password.length() < 8) {
            throw new RuntimeException(BaseExceptions.INVALID_PASSWORD.getMessage());
        }

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private UsernamePasswordAuthenticationToken attemptAuthenticationWithRefreshToken(String refreshToken) {
        VerifyResult result = jwtUtil.verify(refreshToken);
        if (!result.isSuccess()) {
            throw new TokenExpiredException("refresh token expired");
        }

        User user;
        try {
            user = userService.findUserByIdWithAuthorities(result.getUserId());
        } catch (BaseException exception) {
            throw new InvalidClaimException("user info in token claim is not valid");
        }
        return new UsernamePasswordAuthenticationToken(
                user,
                user.getAuthorities()
        );
    }

    private UserLoginDto obtainLoginDto(HttpServletRequest request) throws IOException {
        return objectMapper.readValue(request.getInputStream(), UserLoginDto.class);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        User user = (User) authResult.getPrincipal();
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(AUTH_TOKEN_HEADER_NAME, "Bearer " + jwtUtil.makeAuthToken(user));
        response.setHeader(REFRESH_TOKEN_HEADER_NAME, jwtUtil.makeRefreshToken(user));
    }
}
