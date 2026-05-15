package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 无Javadoc方法
 *
 * @author test-author 2026-04-29
 */
@RestController
@RequestMapping("/api/nojavadoc")
public class NoJavadocController {

    @GetMapping("/get")
    public String getSomething() {
        return "ok";
    }

    @PostMapping("/post")
    public String postSomething() {
        return "ok";
    }

}
