package net.dongliu.requests;

import net.dongliu.requests.auth.Auth;
import net.dongliu.requests.auth.BasicAuth;
import net.dongliu.requests.auth.BearerAuth;
import net.dongliu.requests.body.Part;
import net.dongliu.requests.body.RequestBody;
import net.dongliu.requests.exception.RequestsException;
import net.dongliu.requests.executor.HttpExecutor;
import net.dongliu.requests.executor.RequestExecutorFactory;
import net.dongliu.requests.executor.SessionContext;

import org.jetbrains.annotations.Nullable;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;

/**
 * Http Request builder
 *
 * @author Liu Dong
 */
public final class RequestBuilder {
    String method = Methods.GET;
    URL url;
    Collection<? extends Map.Entry<String, ?>> headers = Collections.emptyList();
    Collection<? extends Map.Entry<String, ?>> cookies = Collections.emptyList();
    String userAgent = DefaultSettings.USER_AGENT;
    Collection<? extends Map.Entry<String, ?>> params = Collections.emptyList();
    Charset charset = StandardCharsets.UTF_8;
    @Nullable
    RequestBody<?> body;
    int socksTimeout = DefaultSettings.SOCKS_TIMEOUT;
    int connectTimeout = DefaultSettings.CONNECT_TIMEOUT;
    @Nullable
    Proxy proxy;
    boolean followRedirect = true;
    int maxRedirectCount = 5;
    boolean compress = true;
    boolean verify = true;
    Auth auth;
    @Nullable
    SessionContext sessionContext;
    boolean keepAlive = true;
    @Nullable
    KeyStore keyStore;

    private List<? extends Interceptor> interceptors = Collections.emptyList();

    RequestBuilder() {
    }

    RequestBuilder(Request request) {
        method = request.getMethod();
        headers = request.getHeaders();
        cookies = request.getCookies();
        userAgent = request.getUserAgent();
        charset = request.getCharset();
        body = request.getBody();
        socksTimeout = request.getSocksTimeout();
        connectTimeout = request.getConnectTimeout();
        proxy = request.getProxy();
        followRedirect = request.isFollowRedirect();
        maxRedirectCount = request.getMaxRedirectCount();
        compress = request.isCompress();
        verify = request.isVerify();
        auth = request.getAuth();
        sessionContext = request.getSessionContext();
        keepAlive = request.isKeepAlive();
        keyStore = request.getKeyStore();
        this.url = request.getUrl();
        this.params = request.getParams();
    }

    public RequestBuilder method(String method) {
        this.method = Objects.requireNonNull(method);
        return this;
    }

    public RequestBuilder url(String url) {
        try {
            this.url = new URL(Objects.requireNonNull(url));
        } catch (MalformedURLException e) {
            throw new RequestsException("Resolve url error, url: " + url, e);
        }
        return this;
    }

    public RequestBuilder url(URL url) {
        this.url = Objects.requireNonNull(url);
        return this;
    }

    /**
     * Set request headers.
     */
    public RequestBuilder headers(Collection<? extends Map.Entry<String, ?>> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Set request headers.
     */
    @SafeVarargs
    public final RequestBuilder headers(Map.Entry<String, ?>... headers) {
        headers(Arrays.asList(headers));
        return this;
    }

    /**
     * Set request headers.
     */
    public final RequestBuilder headers(Map<String, ?> map) {
        this.headers = map.entrySet();
        return this;
    }

    /**
     * Set request cookies.
     */
    public RequestBuilder cookies(Collection<? extends Map.Entry<String, ?>> cookies) {
        this.cookies = cookies;
        return this;
    }

    /**
     * Set request cookies.
     */
    @SafeVarargs
    public final RequestBuilder cookies(Map.Entry<String, ?>... cookies) {
        cookies(Arrays.asList(cookies));
        return this;
    }

    /**
     * Set request cookies.
     */
    public final RequestBuilder cookies(Map<String, ?> map) {
        this.cookies = map.entrySet();
        return this;
    }

    public RequestBuilder userAgent(String userAgent) {
        this.userAgent = Objects.requireNonNull(userAgent);
        return this;
    }

    /**
     * Set url query params.
     */
    public RequestBuilder params(Collection<? extends Map.Entry<String, ?>> params) {
        this.params = params;
        return this;
    }

    /**
     * Set url query params.
     */
    @SafeVarargs
    public final RequestBuilder params(Map.Entry<String, ?>... params) {
        this.params = Arrays.asList(params);
        return this;
    }

    /**
     * Set url query params.
     */
    public final RequestBuilder params(Map<String, ?> map) {
        this.params = map.entrySet();
        return this;
    }

    /**
     * Set charset used to encode request params or forms. Default UTF8.
     */
    public RequestBuilder requestCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Set charset used to encode request params or forms. Default UTF8.
     */
    public RequestBuilder charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Set request body
     */
    public RequestBuilder body(@Nullable RequestBody<?> body) {
        this.body = body;
        return this;
    }

    /**
     * Set www-form-encoded body. Only for Post
     *
     * @deprecated use {@link #body(Collection)} instead
     */
    @Deprecated
    public RequestBuilder forms(Collection<? extends Map.Entry<String, ?>> params) {
        body = RequestBody.form(params);
        return this;
    }

    /**
     * Set www-form-encoded body. Only for Post
     *
     * @deprecated use {@link #body(Map.Entry[])} instead
     */
    @Deprecated
    @SafeVarargs
    public final RequestBuilder forms(Map.Entry<String, ?>... formBody) {
        return forms(Arrays.asList(formBody));
    }

    /**
     * Set www-form-encoded body. Only for Post
     *
     * @deprecated use {@link #body(Map)} instead
     */
    @Deprecated
    public RequestBuilder forms(Map<String, ?> formBody) {
        return forms(formBody.entrySet());
    }


    /**
     * Set www-form-encoded body. Only for Post
     */
    public RequestBuilder body(Collection<? extends Map.Entry<String, ?>> params) {
        body = RequestBody.form(params);
        return this;
    }

    /**
     * Set www-form-encoded body. Only for Post
     */
    @SafeVarargs
    public final RequestBuilder body(Map.Entry<String, ?>... formBody) {
        return body(Arrays.asList(formBody));
    }

    /**
     * Set www-form-encoded body. Only for Post
     */
    public RequestBuilder body(Map<String, ?> formBody) {
        return body(formBody.entrySet());
    }

    /**
     * Set string body
     */
    public RequestBuilder body(String str) {
        body = RequestBody.text(str);
        return this;
    }

    /**
     * Set binary body
     */
    public RequestBuilder body(byte[] bytes) {
        body = RequestBody.bytes(bytes);
        return this;
    }

    /**
     * Set input body
     */
    @Deprecated
    public RequestBuilder body(InputStream input) {
        body = RequestBody.inputStream(input);
        return this;
    }

    /**
     * For send application/json post request.
     * Must have jackson or gson in classpath, or a runtime exception will be raised
     */
    public RequestBuilder jsonBody(Object value) {
        body = RequestBody.json(value);
        return this;
    }

    /**
     * Set tcp socks timeout in milliseconds. Default is 5000
     */
    public RequestBuilder socksTimeout(int timeout) {
        checkTimeout(timeout);
        this.socksTimeout = timeout;
        return this;
    }

    /**
     * Set tcp connect timeout in milliseconds. Default is 3000
     */
    public RequestBuilder connectTimeout(int timeout) {
        checkTimeout(timeout);
        this.connectTimeout = timeout;
        return this;
    }

    private static void checkTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout should not less than 0");
        }
    }

    /**
     * set proxy
     */
    public RequestBuilder proxy(@Nullable Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Set auto handle redirect. default true
     */
    public RequestBuilder followRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
        return this;
    }

    /**
     * Set max redirects count
     */
    public RequestBuilder maxRedirectCount(int maxRedirectCount) {
        this.maxRedirectCount = maxRedirectCount;
        return this;
    }

    /**
     * Set accept compressed response. default true
     */
    public RequestBuilder compress(boolean compress) {
        this.compress = compress;
        return this;
    }

    /**
     * Check ssl cert. default true
     */
    public RequestBuilder verify(boolean verify) {
        this.verify = verify;
        return this;
    }

    /**
     * Add a custom additional keyStore contains X509 Certificate, for ssl connection trust validation.
     */
    public RequestBuilder keyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
        return this;
    }

    /**
     * If reuse http connection. default true
     */
    public RequestBuilder keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    /**
     * Set http basicAuth by BasicAuth(DigestAuth/NTLMAuth not supported now)
     */
    public RequestBuilder basicAuth(String user, String password) {
        this.auth = new BasicAuth(user, password);
        return this;
    }

    /**
     * Set http basicAuth by BasicAuth(DigestAuth/NTLMAuth not supported now)
     */
    public RequestBuilder basicAuth(BasicAuth basicAuth) {
        this.auth = basicAuth;
        return this;
    }

    public RequestBuilder bearerAuth(String token) {
        this.auth = new BearerAuth(token);
        return this;
    }

    public RequestBuilder bearerAuth(BearerAuth bearerAuth) {
        this.auth = bearerAuth;
        return this;
    }

    Request build() {
        return new Request(this);
    }

    /**
     * build http request, and send out
     */
    public RawResponse send() {
        Request request = build();
        RequestExecutorFactory factory = RequestExecutorFactory.getInstance();
        HttpExecutor executor = factory.getHttpExecutor();
        return new InterceptorChain(interceptors, executor).proceed(request);
    }

    /**
     * Set both connect timeout and socks timeout in milliseconds
     */
    public RequestBuilder timeout(int timeout) {
        checkTimeout(timeout);
        return connectTimeout(timeout).socksTimeout(timeout);
    }

    /**
     * Set connect timeout and socks timeout in milliseconds
     */
    public RequestBuilder timeout(int connectTimeout, int socksTimeout) {
        return connectTimeout(connectTimeout).socksTimeout(socksTimeout);
    }

    /**
     * Set multiPart body. Only form multi-part post.
     *
     * @see #multiPartBody(Collection)
     */
    public final RequestBuilder multiPartBody(Part<?>... parts) {
        return multiPartBody(Arrays.asList(parts));
    }

    /**
     * Set multiPart body. Only form multi-part post.
     */
    public RequestBuilder multiPartBody(Collection<Part<?>> parts) {
        return body(RequestBody.multiPart(parts));
    }


    /**
     * Set interceptors
     */
    public RequestBuilder interceptors(List<? extends Interceptor> interceptors) {
        this.interceptors = interceptors;
        return this;
    }

    /**
     * Set interceptors
     */
    public RequestBuilder interceptors(Interceptor... interceptors) {
        return interceptors(Arrays.asList(interceptors));
    }

    /**
     * This method is only for internal use!
     */
    RequestBuilder sessionContext(@Nullable SessionContext sessionContext) {
        this.sessionContext = sessionContext;
        return this;
    }
}