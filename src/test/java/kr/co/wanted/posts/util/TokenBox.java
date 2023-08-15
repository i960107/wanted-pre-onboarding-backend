package kr.co.wanted.posts.util;

import kr.co.wanted.posts.domain.user.User;

public class TokenBox {
    private String authToken;
    private String refreshToken;
    private User user;

    public TokenBox(String authToken, String refreshToken) {
        this.authToken = authToken;
        this.refreshToken = refreshToken;
    }

    public TokenBox(String authToken, String refreshToken, User user) {
        this.authToken = authToken;
        this.refreshToken = refreshToken;
        this.user = user;

    }

    public String getAuthToken() {
        return authToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getUserNickName() {
        return user.getNickName();
    }


}
