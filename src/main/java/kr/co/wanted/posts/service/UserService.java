package kr.co.wanted.posts.service;

import java.util.Optional;
import kr.co.wanted.posts.domain.user.User;
import kr.co.wanted.posts.domain.user.UserAuthority;
import kr.co.wanted.posts.domain.user.UserAuthorityRepository;
import kr.co.wanted.posts.domain.user.UserRepository;
import kr.co.wanted.posts.exception.BaseException;
import kr.co.wanted.posts.exception.BaseException.BaseExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final PasswordEncoder passwordEncoder;


    public User save(User user) throws BaseException {
        if (isDuplicated(user.getEmail())) {
            throw new BaseException(BaseExceptions.USER_EMAIL_DUPLICATE);
        }
        user.encryptPassword(passwordEncoder.encode(user.getPassword()));
        User persistedUser = userRepository.save(user);
        user.addAuthority(UserAuthority.ROLE_USER);
        return persistedUser;
    }

    public User getUserReference(Long userId) {
        return userRepository.getOne(userId);
    }


    private boolean isDuplicated(String email) {
        return userRepository.findByEmailAndEnabled(email, true).isPresent();
    }

    @Transactional(readOnly = true)
    public User findEnabledUserById(Long userId) throws BaseException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty() || !user.get().isEnabled()) {
            throw new BaseException(BaseExceptions.USER__NOT_FOUND);
        }
        return user.get();
    }

    @Transactional(readOnly = true)
    public User findUserByIdWithAuthorities(Long userId) throws BaseException {
        Optional<User> user = userRepository.findByIdWithAuthorities(userId);
        if (user.isEmpty() || !user.get().isEnabled()) {
            throw new BaseException(BaseExceptions.USER__NOT_FOUND);
        }
        return user.get();
    }


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithAuthorities(username)
                .orElseThrow(() -> new UsernameNotFoundException(BaseExceptions.USER__NOT_FOUND.getMessage()));
        return user;
    }

    public void addAuthority(Long userId, String authority) throws BaseException {
        User user = findEnabledUserById(userId);
        user.addAuthority(authority);
    }
}
