package kr.co.wanted.posts.util;

public class TokenBox {
    private String authToken;
    private String refreshToken;

    public TokenBox(String authToken, String refreshToken) {
        this.authToken = authToken;
        this.refreshToken = refreshToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
