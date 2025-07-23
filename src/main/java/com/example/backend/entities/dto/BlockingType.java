package com.example.backend.entities.dto;

public enum BlockingType {
    BLOCAGE("blocage"),
    DEBLOCAGE("deblocage"),
    WHITE_LIST("WhiteList"),
    BLOCAGE_SMS("blocageSMS"),
    DEBLOCAGE_SMS("deblocageSMS");

    private final String value;

    BlockingType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BlockingType fromValue(String value) {
        for (BlockingType type : BlockingType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown blocking type: " + value);
    }
}