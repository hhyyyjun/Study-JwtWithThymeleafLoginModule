package com.example.jwt.controller;

import com.example.jwt.dto.UserDto;
import com.example.jwt.entity.User;
import com.example.jwt.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")  //회원가입, 권한없이 호출 가능
    public ResponseEntity<User> signup(
            @Valid @RequestBody UserDto userDto
    ) {
        return ResponseEntity.ok(userService.signup(userDto)); //http의 body, header, status를 포함한 데이터 -> 추가 서칭 필요
        //Response header에는 웹서버가 웹브라우저에 응답하는 메시지가 들어있음
        //Reponse body에는 데이터 값이 들어있음
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER','ADMIN')") //user와 admin 모두 호출 가능하게 설정
    public ResponseEntity<User> getMyUserInfo() {  //Security Context에 저장되어 있는 인증 정보의 username을 기준으로 한 유저정보 및 권한정보를 리턴
        return ResponseEntity.ok(userService.getMyUserWithAuthorities().get());
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN')")  //admin 권한만 호출 가능
    public ResponseEntity<User> getUserInfo(@PathVariable String username) {  //username 파라미터를 통해 해당 유저의 정보 및 권한 정보 리턴
        return ResponseEntity.ok(userService.getUserWithAuthorities(username).get());
    }
}