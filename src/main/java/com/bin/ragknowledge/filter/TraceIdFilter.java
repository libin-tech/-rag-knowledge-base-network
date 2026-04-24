package com.bin.ragknowledge.filter;

import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.IdUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class TraceIdFilter implements Filter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String CLIENT_IP_HEADER = "X-Real-IP";
    private static final String TRACE_ID_KEY = "traceId";
    private static final String CLIENT_IP_KEY = "clientIp";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = IdUtil.fastSimpleUUID();
        }

        String clientIp = getClientIp(httpRequest);

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(CLIENT_IP_KEY, clientIp);
        httpResponse.setHeader(TRACE_ID_HEADER, traceId);

        try {
            TimeInterval interval = new TimeInterval();
            log.info("Request: {}  from IP: {}， URI:{}", httpRequest.getMethod(), clientIp, httpRequest.getRequestURI());
            chain.doFilter(request, response);
            log.info("Time: {}", interval.intervalPretty());
        } finally {
            MDC.remove(TRACE_ID_KEY);
            MDC.remove(CLIENT_IP_KEY);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(CLIENT_IP_HEADER);
        if (isUnknown(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    private boolean isUnknown(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip);
    }
}