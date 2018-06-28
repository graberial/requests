package net.dongliu.requests.auth;

public class BearerAuth implements Auth {
    private final String token;

    public BearerAuth(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String encode() {
        return "Bearer " + token;
    }
}