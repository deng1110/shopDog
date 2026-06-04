package com.shopdog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * 注册一个 RestTemplate Bean,用来向微信服务器发 HTTP 请求。
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // 微信接口(如 jscode2session)返回 JSON 内容但 Content-Type 标的是 text/plain,
        // 默认 Jackson 转换器只认 application/json,这里让它也接受 text/plain,
        // 否则解析响应时报 UnknownContentTypeException
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        restTemplate.getMessageConverters().add(0, converter);
        return restTemplate;
    }
}
