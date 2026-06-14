package com.example;

public class UsesA {

    public String delegateToA() {
        ModuleA a = new ModuleA();
        return a.hello() + " (via B)";
    }
}
