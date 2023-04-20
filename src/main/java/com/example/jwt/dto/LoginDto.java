package com.example.jwt.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
* @package : com.example.jwt.dto
* @name : LoginDto.java
* @date : 2023-04-19 오후 5:19
* @author : hj
* @Description: 토큰 발급 시 사용
**/
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {

    @NotNull
    @Size(min = 3, max = 50)
    private String username;

    @NotNull
    @Size(min = 3, max = 100)
    private String password;
}