package com.example.demo.controller;

import com.example.demo.service.SayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TestController {

    @Autowired
    private SayService sayService;

    @RequestMapping("/get")
    public String get() {
        return sayService.say();
    }
}
