package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WhiteLeaf03
 */
@RestController
public class MessageController {
    @GetMapping("/message")
    public String message() {
        return "Hello! This is NacosProvider No.2";
    }
}
