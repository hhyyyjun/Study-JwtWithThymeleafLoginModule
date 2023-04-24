package com.example.jwt.controller;

import com.example.jwt.dto.LoginDto;
import com.example.jwt.dto.TokenDto;
import com.example.jwt.jwt.JwtFilter;
import com.example.jwt.jwt.TokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

/**
* @package : com.example.jwt.controller
* @name : AuthController.java
* @date : 2023-04-19 오후 5:06
* @author : hj
* @Description: 권한 관련 클래스
**/
@RestController
@RequestMapping("/api")
public class AuthController {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    /**
    * @methodName : authorize
    * @date : 2023-04-19 오후 5:06
    * @author : hj
    * @Description: 로그인 시 토큰 발급하는 메서드
    **/
    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {
        // form 태그 형식으로 데이터를 전송 받으므로 @RequestBody 불필요
        // 이 프로젝트가 아닌 다른 프로젝트에서 form 미 사용 시 붙이면 됨

        //username과 password 파라미터를 받아 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        //loadUserByUsername 메서드를 통해 유저정보를 조회하여 인증 정보 생성
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        //loadUserByUsername 메서드를 호출하지 않았는데 넘어가는 이유
        //authenticationManangerBuilder.getObject().authenticate() 메소드가 실행되면
        //1. AuthenticationManager 의 구현체인 ProviderManager 의 authenticate() 메소드가 실행
        //2. 해당 메소드에선 AuthenticaionProvider 인터페이스의 authenticate() 메소드를 실행하는데,
        //해당 인터페이스에서 데이터베이스에 있는 이용자의 정보를 가져오는 UserDetailsService 인터페이스를 사용
        //3. 이어서 UserDetailsService 인터페이스의 loadUserByUsername() 메소드를 호출하게 됨
        //따라서 CustomUserDetailsService 구현체에 오버라이드된 loadUserByUsername() 메소드를 호출하게 됨

        //위에서 리턴받은 유저 정보와 권한 정보를 인증 정보를 현재 실행중인 스레드(Security Context)에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //유저정보를 통해 jwt토큰 생성
        String jwt = tokenProvider.createToken(authentication);

        //헤더에 토큰정보를 포함
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new ResponseEntity<>(new TokenDto(jwt), httpHeaders, HttpStatus.OK);
    }
}