package com.lpwoowatpokpt.passportrankingjava.Model;

import java.util.Map;

public class Country {
    private String key;
    private Long value;

    public Country() {
    }

    public Country(String key, Long value) {
        this.key = key;
        this.value = value;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
