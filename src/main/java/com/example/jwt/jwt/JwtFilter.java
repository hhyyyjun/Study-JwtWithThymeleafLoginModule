package com.example.jwt.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
* @package : com.example.jwt.jwt
* @name : JwtFilter.java
* @date : 2023-04-19 오후 5:23
* @author : hj
* @Description: JWT의 인증정보를 SecurityContest에 저장하는 역할을 담당하는 클래스
**/
//토큰 발급 시 헤더에 토큰정보 포함할 때 호출
//JwtFilter의 DoFilter 메서드에서 Request가 들어올 때 SecurityContext에 Authentication 객체를 저장해 사용
public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    //실제 필터링 로직, 토큰의 인증정보를 현재 실행중인 스레드(SecurityContext)에 저장하기 위한 역할 수행
    //토큰 정보가 없거나 유효하지 않으면 정상적으로 수행되지 않음
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        
        // 1. Request Header에서 JWT토큰 추출
        String jwt = resolveToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();
        
        // 2. ValidationToken으로 토큰 유효성 검사
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            // 3. 토큰이 유효할 경우 토큰에서 Authentication 객체를 받아와
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            // 4. SecurityContext에 Authentication 객체 set
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
        } else {
            logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        filterChain.doFilter(servletRequest, servletResponse);
        //요청이 정상적으로 Controller 까지 도착했다면 SecurityContext 에 Member ID 가 존재한다는 것이 보장됨
        //대신 직접 DB 를 조회한 것이 아니라 Access Token 에 있는 User ID 를 꺼냈기 때문에
        //탈퇴로 인해 User ID 가 DB 에 없는 경우 등 예외 상황은 Service 단에서 고려해야 함
    }

    //Request Header 에서 토큰 정보를 꺼내옴
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}