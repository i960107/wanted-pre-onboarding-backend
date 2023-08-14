package kr.co.wanted.posts.config.jwt;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JwtCheckFilter extends BasicAuthenticationFilter {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public JwtCheckFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String bearer = request.getHeader(JwtLoginFilter.AUTH_TOKEN_HEADER_NAME);
        String refreshToken = request.getHeader(JwtLoginFilter.REFRESH_TOKEN_HEADER_NAME);
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        String token = bearer.substring("Bearer ".length());
        VerifyResult result = jwtUtil.verify(token);
        if (result.isSuccess()) {
            User user;
            try {
                user = userService.findUserByIdWithAuthorities(result.getUserId());
            } catch (BaseException exception) {
                throw new InvalidClaimException("user info in token claim is not valid");
            }
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
            chain.doFilter(request, response);
            SecurityContextHolder.clearContext();
        } else {
            throw new TokenExpiredException("token is not valid");
        }
    }
}
