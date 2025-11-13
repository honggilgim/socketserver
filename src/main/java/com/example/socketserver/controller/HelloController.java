package com.example.socketserver.controller;

import com.example.socketserver.service.TcpClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    private TcpClientService tcpClientService;

    @GetMapping("/hello")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from Spring Boot Server!");
        response.put("status", "success");
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return response;
    }

    @GetMapping("/send")
    public Map<String, Object> sendMessage(
            @RequestParam(defaultValue = "192.168.56.101") String host,
            @RequestParam(defaultValue = "9100") int port,
            @RequestParam(defaultValue = "00000000000024AAAABBBB000AAADUMMYTEST") String message) {
        return tcpClientService.sendMessage(host, port, message);
    }
}

