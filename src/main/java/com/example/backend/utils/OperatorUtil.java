package com.example.backend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OperatorUtil {
    @Value("${operator.name}")
    private String operatorName;

    private static final Map<String, String> COUNTRY_PREFIXES = new HashMap<>();
    private static final Map<String, String> COMMAND_PREFIXES = new HashMap<>();

    static {
        // Country prefixes
        COUNTRY_PREFIXES.put("sotelma", "223");
        COUNTRY_PREFIXES.put("expresso", "221");
        COUNTRY_PREFIXES.put("sudatel", "249");
        COUNTRY_PREFIXES.put("moovtogo", "228");
        COUNTRY_PREFIXES.put("chinguitel", "222");
        COUNTRY_PREFIXES.put("mauritel", "222");
        COUNTRY_PREFIXES.put("mattel", "222");
        COUNTRY_PREFIXES.put("tchad", "235");
        COUNTRY_PREFIXES.put("TUN", "216");

        COMMAND_PREFIXES.put("sotelma", "ssh collect@172.20.19.50");
    }

    public String getCurrentOperator() {
        return operatorName;
    }

    public String getCountryPrefix(String operator) {
        return COUNTRY_PREFIXES.getOrDefault(operator, "");
    }

    public String getCommandPrefix(String operator) {
        return COMMAND_PREFIXES.getOrDefault(operator, "");
    }
}