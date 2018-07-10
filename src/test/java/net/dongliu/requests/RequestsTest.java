package net.dongliu.requests;

import com.alibaba.fastjson.JSONObject;
import net.dongliu.requests.body.InputStreamSupplier;
import net.dongliu.requests.body.Part;
import net.dongliu.requests.json.TypeInfer;
import net.dongliu.requests.mock.MockServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;

import static org.junit.Assert.*;

public class RequestsTest {

    private static MockServer server = new MockServer();

    @BeforeClass
    public static void init() {
        server.start();
    }

    @AfterClass
    public static void destroy() {
        server.stop();
    }

    @Test
    public void testGet() {
        String resp = Requests.get("http://127.0.0.1:8080")
                .requestCharset(StandardCharsets.UTF_8).send().readToText();
        assertFalse(resp.isEmpty());

        resp = Requests.get("http://127.0.0.1:8080").send().readToText();
        assertFalse(resp.isEmpty());

        // get with params
        Map<String, String> map = new HashMap<>();
        map.put("wd", "test");
        resp = Requests.get("http://127.0.0.1:8080").params(map).send().readToText();
        assertFalse(resp.isEmpty());
        assertTrue(resp.contains("wd=test"));
    }

    @Test
    public void testHead() {
        RawResponse resp = Requests.head("http://127.0.0.1:8080")
                .requestCharset(StandardCharsets.UTF_8).send();
        assertEquals(200, resp.getStatusCode());
        String statusLine = resp.getStatusLine();
        assertEquals("HTTP/1.1 200 OK", statusLine);
        String text = resp.readToText();
        assertTrue(text.isEmpty());
    }

    @Test
    public void testPost() {
        // form encoded post
        String text = Requests.post("http://127.0.0.1:8080/post")
                .body(Parameter.of("wd", "test"))
                .send().readToText();
        assertTrue(text.contains("wd=test"));
    }

    @Test
    public void testCookie() {
        Response<String> response = Requests.get("http://127.0.0.1:8080/cookie")
                .cookies(Parameter.of("test", "value")).send().toTextResponse();
        boolean flag = false;
        for (Cookie cookie : response.getCookies()) {
            if (cookie.getName().equals("test")) {
                flag = true;
                break;
            }
        }
        assertTrue(flag);
    }

    @Test
    public void testBasicAuth() {
        Response<String> response = Requests.get("http://127.0.0.1:8080/basicAuth")
                .basicAuth("test", "password")
                .send().toTextResponse();
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testBearerAuth() {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJyIl0sImV4cCI6MTUzMDIxNzI4OSwianRpIjoiM2I1Zjk5MjktNzU3OS00ODI1LWJjN2ItOWJhZjY2YTEyNTI2IiwiY2xpZW50X2lkIjoieHpsaWJ0ZXN0IiwiZ3JvdXBDb2RlIjoiMDAwMDExMTAxMCIsIm1hcHBpbmdQYXRoIjoieHpsaWIifQ.XU-_oQVHoNKpnxLvObkXoxD9v-xcmt837rekEUGExgREboL7B3XSAUFTGQUCTU2IBl_eo5cv-diQyPTFp9sVTT1_RpSsq7ZaJrj6jPzQLMrfp5pBAgc_LlG6PgMeXfQHHI-U5v7HOzITV61c69jsjP9Rf3RqisVPWFxIRw0eGLF0X-ZyehJaNyoU85U1YV8Mer9Ib-yQQEiG3SoIlI4eLdDJ6gNl5LKNDGoVJfdWvIJ4rcp4SqSa8N2ZNt0eyvgNjM-u5iAWpxNMZcZI7RqGUcriM3uNT6r2euaDiLmq1Ty_3NUU5_CQdf2jeicREIXHphrFxNqdwM2dcDitlUvJvg";
        Response<String> response = Requests.post("https://open.libstar.cn/fu/loa/bookInfo/searchNewBook")
                .bearerAuth(token)
                .headers(Collections.singletonMap("Content-Type", "application/json"))
                .jsonBody(JSONObject.parse("{ \n" +
                        "\t\"days\":200,\n" +
                        "\t\"page\": 1,\n" +
                        "\t\"rows\": 20\n" +
                        "}"))
                .send().toTextResponse();
        System.out.println(response.getBody());
    }

    @Test
    public void testRedirect() {
        Response<String> resp = Requests.get("http://127.0.0.1:8080/redirect").userAgent("my-user-agent")
                .send().toTextResponse();
        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getBody().contains("/redirected"));
        assertTrue(resp.getBody().contains("my-user-agent"));
    }

    @Test
    public void testMultiPart() {
        String body = Requests.post("http://127.0.0.1:8080/multi_part")
                .multiPartBody(Part.file("writeTo", "keystore", new InputStreamSupplier() {
                    @Override
                    public InputStream get() {
                        return this.getClass().getResourceAsStream("/keystore");
                    }
                }).contentType("application/octem-stream"))
                .send().readToText();
        assertTrue(body.contains("writeTo"));
        assertTrue(body.contains("application/octem-stream"));
    }


    @Test
    public void testMultiPartText() {
        String body = Requests.post("http://127.0.0.1:8080/multi_part")
                .multiPartBody(Part.text("test", "this is test value"))
                .send().readToText();
        assertTrue(body.contains("this is test value"));
        assertTrue(!body.contains("plain/text"));
    }

    @Test
    public void sendJson() {
        String text = Requests.post("http://127.0.0.1:8080/echo_body").jsonBody(Arrays.asList(1, 2, 3))
                .send().readToText();
        assertTrue(text.startsWith("["));
        assertTrue(text.endsWith("]"));
    }

    @Test
    public void receiveJson() {
        List<Integer> list = Requests.post("http://127.0.0.1:8080/echo_body").jsonBody(Arrays.asList(1, 2, 3))
                .send().readToJson(new TypeInfer<List<Integer>>() {
                });
        assertEquals(3, list.size());
    }

    @Test
    public void sendHeaders() {
        String text = Requests.get("http://127.0.0.1:8080/echo_header")
                .headers(new Header("Host", "www.test.com"), new Header("TestHeader", 1))
                .send().readToText();
        assertTrue(text.contains("Host: www.test.com"));
        assertTrue(text.contains("TestHeader: 1"));
    }

    @Test
    public void testHttps() {
        Response<String> response = Requests.get("https://127.0.0.1:8443/https")
                .verify(false).send().toTextResponse();
        assertEquals(200, response.getStatusCode());


        KeyStore keyStore = KeyStores.load(this.getClass().getResourceAsStream("/keystore"), "123456".toCharArray());
        response = Requests.get("https://127.0.0.1:8443/https")
                .keyStore(keyStore)
                .send().toTextResponse();
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testInterceptor() {
        final long[] statusCode = {0};
        Interceptor interceptor = new Interceptor() {
            @Override
            public RawResponse intercept(InvocationTarget target, Request request) {
                RawResponse response = target.proceed(request);
                statusCode[0] = response.getStatusCode();
                return response;
            }
        };

        String text = Requests.get("http://127.0.0.1:8080/echo_header")
                .interceptors(interceptor)
                .send().readToText();
        assertFalse(text.isEmpty());
        assertTrue(statusCode[0] > 0);
    }
}