package com.shopdog.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 统一打印请求入参与响应返回的过滤器。
 *
 * <p>参考 promotion-management 的 LogCommon2Filter,但去掉了公司内部工具依赖,
 * 改用 Spring 自带的 ContentCaching 包装器,从而能真正抓到请求体和响应体。
 */
@Slf4j
@Component
public class LogCommon2Filter extends OncePerRequestFilter {

    /** 超过该毫秒数视为慢请求 */
    private static final long SLOW_MILLIS = 200;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 包装请求/响应,使其内容可被重复读取(否则 body 读一次就没了)
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);

        long beginTime = System.currentTimeMillis();
        String path = request.getRequestURI();
        String method = request.getMethod();
        String query = request.getQueryString();

        try {
            chain.doFilter(req, resp);
        } finally {
            long consumeTime = System.currentTimeMillis() - beginTime;

            // body 必须在 chain 执行后取:此时下游已读过请求体、写过响应体
            String reqBody = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8);
            String respBody = new String(resp.getContentAsByteArray(), StandardCharsets.UTF_8);

            log.info("request  path[{}] method[{}] query[{}] body[{}]", path, method, query, reqBody);
            log.info("response path[{}] status[{}] cost[{}ms] body[{}]", path, resp.getStatus(), consumeTime, respBody);
            if (consumeTime > SLOW_MILLIS) {
                log.warn("slow request path[{}] cost[{}ms]", path, consumeTime);
            }

            // 关键:把缓存的响应体写回真正的输出流,否则客户端收不到任何内容
            resp.copyBodyToResponse();
        }
    }
}
