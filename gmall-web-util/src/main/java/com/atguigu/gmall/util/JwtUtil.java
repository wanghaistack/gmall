package com.atguigu.gmall.util;

import io.jsonwebtoken.*;

import java.util.Map;

public class JwtUtil {
    //需要key.map,颜值三个组成部分
    //3、	签名部分
    //根据用户信息+盐值+密钥生成的签名。
    public static String encode(String key, Map<String, Object> param, String salt) {
        if (salt != null && salt.length() > 0) {
            key += salt;
        }
        //生成签名部分
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder = jwtBuilder.setClaims(param);
        //获取生成的token
        String token = jwtBuilder.compact();
        return token;
    }

    public static Map<String, Object> decode(String key,String token , String salt) throws SignatureException{
        Claims claims = null;
        if (salt != null && salt.length() > 0) {
            key += salt;
        }

        claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        return claims;
    }
}
