package com.example.backend.utils;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomP6SpyMessageFormatter implements MessageFormattingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CustomP6SpyMessageFormatter.class);
    private static final ThreadLocal<String> currentFullQuery = new ThreadLocal<>();

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }
        // Format SQL (this should include parameter values if configured)
        String formatted = sql.replaceAll("\\s+", " ").trim();
        // Log to help troubleshoot
        logger.debug("Intercepted SQL: {}", formatted);
        // Store in ThreadLocal for retrieval in AuditLogListener
        currentFullQuery.set(formatted);
        return formatted;
    }

    public static String getCurrentFullQuery() {
        return currentFullQuery.get();
    }

    public static void clear() {
        currentFullQuery.remove();
    }
}
