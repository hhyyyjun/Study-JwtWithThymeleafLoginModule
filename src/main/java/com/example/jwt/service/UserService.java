package com.example.jwt.service;


import com.example.jwt.dto.UserDto;
import com.example.jwt.entity.Authority;
import com.example.jwt.entity.User;
import com.example.jwt.repository.UserRepository;
import com.example.jwt.utils.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
    * @methodName : signup
    * @date : 2023-04-20 오후 1:02
    * @author : hj
    * @Description: 회원가입 시 호출되는 메서드
    **/
    @Transactional
    public User signup(UserDto userDto) {  //username을 통해 이미 가입되어 있는지 확인
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            // .orElse : optional에 들어갈 값이 null일 경우 orElse 안의 내용을 실행
            // Optional이란? 자바에서 Null 참조시 NullPointerException을 방지해주는 클래스
            throw new RuntimeException("이미 가입되어 있는 유저입니다.");
        }

        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")  //권한을 USER 설정
                .build();

        User user = User.builder() //유저 정보 빌드
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickname(userDto.getNickname())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        return userRepository.save(user);  // save = DB에 insert
    }

    /**
    * @methodName : getMyUserWithAuthorities
    * @date : 2023-04-20 오후 1:02
    * @author : hj
    * @Description: SecurityUtil의 getCurrentUsername 메소드가 리턴하는 username의 유저 권한 및 권한 정보 리턴
    **/
    @Transactional(readOnly = true)
    public Optional<User> getMyUserWithAuthorities() {
        return SecurityUtil.getCurrentUsername().flatMap(userRepository::findOneWithAuthoritiesByUsername);
    }

    /**
    * @methodName : getUserWithAuthorities
    * @date : 2023-04-20 오후 1:02
    * @author : hj
    * @Description: username을 통해 해당 유저의 정보 및 권한 정보 리턴
    **/
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username);
    }

}