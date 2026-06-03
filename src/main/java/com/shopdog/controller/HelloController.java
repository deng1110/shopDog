package com.shopdog.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;

/**
 * 受保护的业务接口:校验请求头里的 JWT,验签通过才返回数据。
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @GetMapping("/hello")
    public ResponseEntity<?> hello(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "缺少 token"));
        }
        String token = authorization.substring("Bearer ".length());

        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String openid = claims.getSubject();
            return ResponseEntity.ok(Collections.singletonMap("msg", "hello, " + openid));
        } catch (Exception e) {
            // 验签失败、过期、格式错误都走这里
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "token 无效或已过期"));
        }
    }
}
