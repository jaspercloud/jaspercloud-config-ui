package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RefreshScope
public class TestController {

    private String text;

    @Value("${text}")
    public void setText(String text) {
        this.text = text;
    }

    @RequestMapping("/get")
    public String get() {
        return text;
    }
}
