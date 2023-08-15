package kr.co.wanted.posts.config;


import kr.co.wanted.posts.config.jwt.JwtCheckFilter;
import kr.co.wanted.posts.config.jwt.JwtLoginFilter;
import kr.co.wanted.posts.config.jwt.JwtUtil;
import kr.co.wanted.posts.exception.ExceptionHandlerFilter;
import kr.co.wanted.posts.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserService userService;
    private final JwtUtil jwtUtil;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JwtLoginFilter loginFilter = new JwtLoginFilter(authenticationManager(), jwtUtil, userService);
        JwtCheckFilter checkFilter = new JwtCheckFilter(authenticationManager(), jwtUtil, userService);
        ExceptionHandlerFilter exceptionHandlerFilter = new ExceptionHandlerFilter();

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(checkFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(exceptionHandlerFilter, JwtCheckFilter.class)
                .addFilterBefore(exceptionHandlerFilter, JwtLoginFilter.class)
                .csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().requestMatchers(
                PathRequest.toH2Console(),
                PathRequest.toStaticResources().atCommonLocations()
        );
    }
}
