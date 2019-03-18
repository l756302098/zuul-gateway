package com.gateway.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.ClaimsHolder;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gateway.model.Result;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    /**
     * APP登录Token的生成和解析
     *
     */
    public static final String KEY = "TxOQdaQt7oZ3PgdyT8UvRNQ4UEJJPJwW";
    /** token秘钥，请勿泄露，请勿随便修改  */
    public static final String SECRET = "4GY110tuZX6$xAMWxgF#fR@1g%3FFo#4";
    /** token 过期时间: 30分钟 */
    public static final int calendarField = Calendar.MINUTE;
    public static final int calendarInterval = 2 * 60 * 1000;

    /**
     * JWT生成Token.<br/>
     *
     * JWT构成: header, payload, signature
     *
     * @param userId
     *            登录成功后用户user_id, 参数user_id不可传空
     */
    public static String createToken(Integer userId) throws Exception {
        Date iatDate = new Date();
        // expire time
//        Calendar nowTime = Calendar.getInstance();
//        nowTime.add(calendarField, calendarInterval);
//        Date expiresDate = nowTime.getTime();

        // header Map
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");

        // build token
        // param backups {iss:Service, aud:APP}
        String token = JWT.create().withHeader(map) // header
                .withClaim("iss", KEY) // payload
                .withClaim("aud", "kwd")
                .withClaim("userId", userId)
                .withIssuedAt(iatDate) // sign time
                .sign(Algorithm.HMAC256(SECRET)); // signature

        return token;
    }

    public static String createToken(Integer userId,String key,String secret) throws Exception {
        Date iatDate = new Date();
        // expire time
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(calendarField, calendarInterval);
        Date expiresDate = nowTime.getTime();

        // header Map
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");

        // build token
        // param backups {iss:Service, aud:APP}
        String token = JWT.create().withHeader(map) // header
                .withClaim("iss", key) // payload
                .withClaim("aud", "kwd")
                .withClaim("userId", userId)
                .withIssuedAt(iatDate) // sign time
                .withExpiresAt(expiresDate) // expire time
                .sign(Algorithm.HMAC256(secret)); // signature

        return token;
    }

    /**
     * 验证Token
     *
     * @param token
     * @return
     * @throws Exception
     */
    public static String verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            DecodedJWT jwt = verifier.verify(token);
        } catch (JWTVerificationException e) {
            e.getMessage();
            System.out.println("Message:"+e.getMessage());
            //e.printStackTrace();
            // token 校验失败, 抛出Token验证非法异常
            return e.getMessage();
        }
        return null;
    }

    /**
     * 根据Token获取user_id
     *
     * @param token
     *
     */
    public static Result getUID(String token) {
        Result result = new Result();
        result.setCode(GatewayCodeEnum.TOKEN_INVALID.getValue());
        DecodedJWT jwt = null;
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            jwt = verifier.verify(token);
        }
        catch (JWTVerificationException e) {
            e.printStackTrace();
            // token 校验失败, 抛出Token验证非法异常
            result.setMsg(e.getMessage());
            return result;
        }
        //验证是否过期
        Date issuedAt = jwt.getIssuedAt();
        long diff = (new Date()).getTime() - issuedAt.getTime();
        System.out.println("issuedAt:"+issuedAt+ " diff:"+diff);
        if(diff>calendarInterval){
            result.setCode(GatewayCodeEnum.TOKEN_EXPIRE.getValue());
            return result;
        }

        Map<String, Claim> claims = jwt.getClaims();
        if(claims==null) {
            return result;
        }
        Claim user_id_claim = claims.get("userId");
        if (null == user_id_claim) {
            // token 校验失败, 抛出Token验证非法异常
            return result;
        }
        result.setCode(GatewayCodeEnum.OK.getValue());
        result.setData(user_id_claim.asInt());
        return result;
    }

}
