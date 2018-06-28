package net.dongliu.requests.auth;

import net.dongliu.requests.utils.Base64;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Http Basic Authentication
 *
 * @author Liu Dong
 */
public class BasicAuth implements Auth {
    private static final long serialVersionUID = 7453526434365174929L;
    private final String user;
    private final String password;

    public BasicAuth(String user, String password) {
        this.user = Objects.requireNonNull(user);
        this.password = Objects.requireNonNull(password);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    /**
     * Encode to http header
     */
    @Override
    public String encode() {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(UTF_8));
    }

}
