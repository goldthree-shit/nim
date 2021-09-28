package com.justafewmistakes.nim.common.util;

import com.justafewmistakes.nim.common.api.ResultEnums;
import com.justafewmistakes.nim.common.excpetion.FailEnums;
import com.justafewmistakes.nim.common.excpetion.IMException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author justafewmistakes
 * @version 1.0
 * @date 2021/2/23 22:50
 */
@Component
public class TokenUtil {
    /**
     * 创建秘钥
     */
    private static final byte[] SECRET = "6MNSobBRCHGIO0fS6MNSobBRCHGIO0fS".getBytes();

    /**
     * 过期时间两小时
     */
    private static final long EXPIRE_TIME = 10000000000000L;


    /**
     * 生成Token
     */
    public String buildJWT(String userId,String username, String gateway) {
        try {
            /**
             * 1.创建一个32-byte的密匙
             */
            MACSigner macSigner = new MACSigner(SECRET);
            /**
             * 2. 建立payload 载体
             */
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject("nim")
                    .issuer("http://www.doiduoyi.com")
//                    .expirationTime(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                    .claim("userId",userId)
                    .claim("username",username)
                    .claim("gateway",gateway)
                    .build();

            /**
             * 3. 建立签名
             */
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(macSigner);

            /**
             * 4. 生成token
             */
            String token = signedJWT.serialize();
            return token;
        } catch (KeyLengthException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取jwt中对应的数据
     * 已经被废弃，不太好用，建议直接返回一个map，这样取用比较方便，当然只希望取用单个的时候用这个会比较方便
     */
    @Deprecated
    public String getInfoFromToken(String token, String expect) throws Exception{
        SignedJWT jwt = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SECRET);
        //校验是否有效
        if (!jwt.verify(verifier)) {
            throw new IMException(Integer.parseInt(String.valueOf(FailEnums.UNAUTHORIZED.getCode())),"token无效");
        }

        //获取载体中的数据
        Object account = jwt.getJWTClaimsSet().getClaim(expect);
        //token中不存在该值，抛出异常
        if (Objects.isNull(account)){
            throw new IMException(Integer.parseInt(String.valueOf(FailEnums.UNAUTHORIZED.getCode())),expect + "为空,请重新登录");
        }
        return (String) account;
    }

    /**
     * 获取jwt中全部的数据
     */
    public Map<String, String> getInfoMapFromToken(String token) throws Exception{
        Map<String, String> map = new HashMap<>();
        String[] claimExpects = new String[]{"userId", "username", "gateway"};

        SignedJWT jwt = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SECRET);
        //校验是否有效
        if (!jwt.verify(verifier)) {
            throw new IMException(Integer.parseInt(String.valueOf(FailEnums.UNAUTHORIZED.getCode())),"token无效");
        }

        for (String expect:claimExpects) {
            //获取载体中的数据
            Object account = jwt.getJWTClaimsSet().getClaim(expect);
            //token中不存在该值，抛出异常
            if (Objects.isNull(account)){
                throw new IMException(Integer.parseInt(String.valueOf(FailEnums.UNAUTHORIZED.getCode())),expect + "为空,请重新登录");
            }
            map.put(expect, (String) account);
        }

        return map;
    }

    /**
     * 校验token
     * @param token
     * @return
     */
    public boolean validateToken(String token) throws Exception{
        SignedJWT jwt = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SECRET);
        //校验是否有效
        if (!jwt.verify(verifier)) {
            throw new IMException(Integer.parseInt(String.valueOf(FailEnums.UNAUTHORIZED.getCode())),"token无效");
        }

        //校验超时
//        Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
//        if (new Date().after(expirationTime)) {
//            throw new BaseException(Integer.parseInt(String.valueOf(ResultCode.UNAUTHORIZED.getCode())), "token过期,请重新登录");
//        }

        //获取载体中的数据
        Object account = jwt.getJWTClaimsSet().getClaim("userId");
        //是否有openUid
        if (Objects.isNull(account)){
            throw new IMException(Integer.parseInt(String.valueOf(FailEnums.UNAUTHORIZED.getCode())),"账号为空,请重新登录");
        }
        return true;
    }


    /**
     * 解析userId账号
     * @param token
     * @return
     * @throws Exception
     */
    public String parseAccount(String token,String claimName) throws Exception
    {
        SignedJWT jwt = SignedJWT.parse(token);
        Object claim = jwt.getJWTClaimsSet().getClaim(claimName);
        return claim.toString();
    }
}
