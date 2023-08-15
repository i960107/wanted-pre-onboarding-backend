package kr.co.wanted.posts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ComponentScan(basePackages = {
        "kr.co.wanted.posts.service",
        "kr.co.wanted.posts.web",
        "kr.co.wanted.posts.exception",
})
@EnableJpaRepositories(basePackages = {
        "kr.co.wanted.posts.domain"
})
public class WebConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
