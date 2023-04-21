package com.example.jwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/prac")
public class PageController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("hello");
    }

    @GetMapping("/main")
    public String main(){
        System.out.println("메인화면");
        return "main/main";
    }
    
    @GetMapping("/content")
    public String content(Principal principal){
        System.out.println(principal.getName());
        return "content";
    }

}
