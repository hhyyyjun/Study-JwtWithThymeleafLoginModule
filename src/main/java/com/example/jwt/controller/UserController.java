package com.example.jwt.controller;

import com.example.jwt.dto.UserDto;
import com.example.jwt.entity.User;
import com.example.jwt.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
* @package : com.example.jwt.controller
* @name : UserController.java
* @date : 2023-04-19 오후 5:18
* @author : hj
* @Description: User 관련 클래스
**/
@Controller
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
    * @methodName : signup
    * @date : 2023-04-19 오후 5:18
    * @author : hj
    * @Description: 회원가입 메서드
    **/
    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<User> signup(
            @Valid @RequestBody UserDto userDto
    ) {
        return ResponseEntity.ok(userService.signup(userDto)); //http의 body, header, status를 포함한 데이터 -> 추가 서칭 필요
        //Response header에는 웹서버가 웹브라우저에 응답하는 메시지가 들어있음
        //Reponse body에는 데이터 값이 들어있음
    }

    /**
    * @methodName : signin
    * @date : 2023-04-20 오전 10:42
    * @author : hj
    * @Description: 로그인 페이지로 이동
    **/
    @GetMapping("/signin")
    public String signin(){
        return "auth/login";
    }

    /**
    * @methodName : login
    * @date : 2023-04-20 오전 10:43
    * @author : hj
    * @Description:
    **/
//    @PostMapping
//    @ResponseBody
//    public ResponseEntity<User> login(@Valid @RequestBody UserDto userDto){
//
//        return false;
//    }

    /**
    * @methodName : getMyUserInfo
    * @date : 2023-04-19 오후 5:18
    * @author : hj
    * @Description: 권한에 따라 User 정보 출력
    **/
    @GetMapping("/user")
    @ResponseBody
    @PreAuthorize("hasAnyRole('USER','ADMIN')") //user와 admin 모두 호출 가능하게 설정
    public ResponseEntity<User> getMyUserInfo() {  //Security Context에 저장되어 있는 인증 정보의 username을 기준으로 한 유저정보 및 권한정보를 리턴
        return ResponseEntity.ok(userService.getMyUserWithAuthorities().get());
    }

    @GetMapping("/user/{username}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN')")  //admin 권한만 호출 가능
    public ResponseEntity<User> getUserInfo(@PathVariable String username) {  //username 파라미터를 통해 해당 유저의 정보 및 권한 정보 리턴
        return ResponseEntity.ok(userService.getUserWithAuthorities(username).get());
    }
}