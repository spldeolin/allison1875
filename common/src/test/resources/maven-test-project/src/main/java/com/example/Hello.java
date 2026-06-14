package com.example;

import org.apache.commons.lang3.StringUtils;

public class Hello {

    public String greet(String name) {
        if (StringUtils.isBlank(name)) {
            return "Hello, World!";
        }
        return "Hello, " + name + "!";
    }
}
