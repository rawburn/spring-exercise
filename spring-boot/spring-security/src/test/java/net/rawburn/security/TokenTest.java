package net.rawburn.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.Test;

import java.util.Date;

/**
 * @author rawburn·rc
 */
public class TokenTest {

    private static final String APP_ID      = "YOUR_APP_ID";
    private static final String APP_KEY     = "YOUR_APP_KEY";

    @Test
    public void test() throws InterruptedException {
        // from client
        String s = buildToken();
        System.out.println(s);

        Thread.sleep(1000);

        String s1 = buildToken();
        System.out.println(s1);

        System.out.println(s.equals(s1));
    }

    /**
     * 生成一个 60 秒内有效的 token，具体有效期时间请根据自身实际情况酌情调整
     */
    public static String buildToken() throws JWTCreationException {
        Algorithm algorithm = Algorithm.HMAC256(APP_KEY);
        return JWT.create()
                .withIssuer(APP_ID)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60000))
                .sign(algorithm);
    }

    /**
     * 对给定的 token 字串进行校验，如失败则抛出异常
     */
    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(APP_KEY);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(APP_ID)
                .build();

        return verifier.verify(token);
    }
}