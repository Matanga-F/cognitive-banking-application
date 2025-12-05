package com.cognitive.banking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class BankController {

    @GetMapping("/state")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Cognitive Bank API is alive");
    }
}

