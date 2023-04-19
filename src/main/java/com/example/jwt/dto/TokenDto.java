package com.example.jwt.dto;

import lombok.*;

//클라이언트에 토큰을 보내기 위한 DTO
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDto {
    private String token;
}