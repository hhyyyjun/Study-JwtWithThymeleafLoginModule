package com.example.jwt.dto;

import lombok.*;


/**
* @package : com.example.jwt.dto
* @name : TokenDto.java
* @date : 2023-04-19 오후 5:20
* @author : hj
* @Description: 클라이언트에 보내기 위한 dto
**/

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDto {
    private String token;
}