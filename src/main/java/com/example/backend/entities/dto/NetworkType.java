package com.example.backend.entities.dto;

public enum NetworkType {
    ONNET("onnet"),
    OFFNET("offnet");

    private final String value;

    NetworkType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NetworkType fromValue(String value) {
        for (NetworkType type : NetworkType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown network type: " + value);
    }
}