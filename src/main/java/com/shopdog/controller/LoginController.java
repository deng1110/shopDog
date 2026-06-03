package com.shopdog.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * 登录接口:把前端的临时 code 换成用户身份(openid),再签发 JWT 返回。
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    @Value("${wx.appid}")
    private String appId;

    @Value("${wx.secret}")
    private String appSecret;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final RestTemplate restTemplate;

    public LoginController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String code = body == null ? null : body.get("code");
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "code 不能为空"));
        }

        // 1. 拿 code + AppID + AppSecret 去微信换取用户身份
        String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + appId
                + "&secret=" + appSecret
                + "&js_code=" + code
                + "&grant_type=authorization_code";

        Map<?, ?> wxResp = restTemplate.getForObject(url, Map.class);
        if (wxResp == null || wxResp.get("openid") == null) {
            // 微信返回 errcode/errmsg 时走这里(如 code 失效、AppSecret 错误等)
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "微信换取 openid 失败: " + wxResp));
        }
        String openid = (String) wxResp.get("openid");

        // (真实项目:此处用 openid 查/建用户,拿到自己系统的 userId;demo 直接用 openid)

        // 2. 用 openid 签发 JWT(有效期 1 天)
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject(openid)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}
