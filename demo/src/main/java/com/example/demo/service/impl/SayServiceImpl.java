package com.example.demo.service.impl;

import com.example.demo.service.SayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@RefreshScope
public class SayServiceImpl implements SayService {

    private String text;

    @Value("${text}")
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String say() {
        return text;
    }
}
