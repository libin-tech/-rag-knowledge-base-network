package com.bin.ragknowledge.context;

/**
 * 链路追踪上下文
 * <p>
 * 用于在当前线程中存储 traceId 等链路信息，
 * 实现全链路追踪。
 */
public class TraceContext {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    public static String getTraceId() {
        return TRACE_ID.get();
    }

    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static void remove() {
        TRACE_ID.remove();
    }
}