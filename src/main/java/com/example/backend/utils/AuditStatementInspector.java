package com.example.backend.utils;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class AuditStatementInspector implements StatementInspector {
    private static final ThreadLocal<String> currentSql = new ThreadLocal<>();

    @Override
    public String inspect(String sql) {
        currentSql.set(sql);
        return sql;
    }

    public static String getCurrentSql() {
        return currentSql.get();
    }

    public static void clear() {
        currentSql.remove();
    }
}