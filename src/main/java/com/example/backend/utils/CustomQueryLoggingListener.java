package com.example.backend.utils;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import java.util.List;

public class CustomQueryLoggingListener implements QueryExecutionListener {

    private static final ThreadLocal<String> currentFullQuery = new ThreadLocal<>();

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        // No action required before query execution.
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        // Concatenate all queries (if more than one) into one string.
        StringBuilder sb = new StringBuilder();
        for (QueryInfo info : queryInfoList) {
            sb.append(info.getQuery()).append(" ");
        }
        String fullQuery = sb.toString().trim();
        currentFullQuery.set(fullQuery);
    }

    public static String getCurrentFullQuery() {
        return currentFullQuery.get();
    }

    public static void clear() {
        currentFullQuery.remove();
    }
}
