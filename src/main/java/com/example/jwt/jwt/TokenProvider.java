package com.example.jwt.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

//토큰의 생성, 토큰의 유효성 검증, 암호화 설정 등의 역할을 담당하는 클래스
@Component
public class TokenProvider implements InitializingBean {
    //InitializingBean을 implements 받아 afterPropertiesSet을 Override 하는 이유는
    //TokenProvider Bean이 생성되고, 주입을 받은 후에 secret 값을 Base64 Decode해서 key변수에 할당하기 위함

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";

    private final String secret;
    private final long tokenValidityInMilliseconds;

    private Key key;


    public TokenProvider(    //application.yml에서 정의한 header와 validity 값 주입
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    //빈이 생성이 되고 의존성 주입 이후에 secret값을 Base64 Decode해서 key 변수에 할당하기 위함
    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    //유저 정보를 가지고 AccessToken을 생성하는 메서드
    //Authentication 객체에 포함되어 있는 권한 정보들을 담은 토큰 생성
    public String createToken(Authentication authentication) {
        
        //권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime(); //현재 시간을 밀리초로 환산

        //현재 시간과 yml 파일에서 설정한 토큰 만료시간을 붙임
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        //토큰 생성하여 리턴
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities) //JWT의 body이고 key-value 데이터를 추가함. 여기서는 권한정보
                .setExpiration(validity)  //만료일 설정
                .signWith(key, SignatureAlgorithm.HS512) //HS512 알고리즘 적용
                .compact(); //토큰 생성
    }

    //JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    //토큰에 담겨있는 권한 정보들을 이용해 Authentication 객체를 리턴
    //JwtFilter에서 사용됨
    public Authentication getAuthentication(String token) {
        
        //토큰 복호화
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        //클레임에서 권한 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        //UserDetails 객체를 만들어서 Authentication 리턴
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    //토큰 정보 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}