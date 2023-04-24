package com.example.jwt.config;

import com.example.jwt.jwt.JwtAccessDeniedHandler;
import com.example.jwt.jwt.JwtAuthenticationEntryPoint;
import com.example.jwt.jwt.JwtSecurityConfig;
import com.example.jwt.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
* @package : com.example.jwt.config
* @name : SecurityConfig.java
* @date : 2023-04-19 오후 5:17
* @author : hj
* @Description: Spring Security에서 사용할 보안 설정을 하는 클래스
**/
@EnableWebSecurity //기본적인 Web 보안 활성화
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            TokenProvider tokenProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) {
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer configure(){
        return (web) ->
        web
                .ignoring()
                .antMatchers(  //아래의 요청들에 대해서는 Spring Security 로직을 수행하지 않아도 접근이 가능(=인증 무시)
                        "/h2-console/**"
                        ,"/favicon.ico"
                        ,"/css/**", "/js/**", "/img/**"
                );
    }

    //WebSecurityConfigurerAdapter는 더이상 사용되지 않아 SecurityFilterChain을 Bean으로 등록하여 사용
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  //token 방식이므로 csrf 설정 x

                .exceptionHandling() //예외처리 지정 401, 403
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                .and() // 데이터 확인을 위한 h2 console 설정 추가 -> 추가적인 서칭 필요
                    .headers()
                    .frameOptions()
                    .sameOrigin()

                .and() //Security는 기본적으로 세션을 사용하지만 jwt는 세션을 사용하지 않으므로 STATELESS로 설정
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                    .authorizeRequests()  //요청들에 대한 접근 설정
                    .antMatchers("/prac/*").permitAll()  //해당 Path 요청들은 인증없이 접근 설정
                    .antMatchers("/api/*").permitAll()
                    .antMatchers("/static/*").permitAll()
//                    .antMatchers("/api/authenticate").permitAll()
//                    .antMatchers("/api/signup").permitAll()
//                    .antMatchers("/api/signin").permitAll()
//                    .antMatchers("/api/login").permitAll()
                    .anyRequest().authenticated()  //이외 나머지 요청은 인증이 필요

                .and() // JwtFilter를 addFilterBefore로 등록했던 JwtSecurityConfig 클래스를 적용
                    .apply(new JwtSecurityConfig(tokenProvider));

        return http.build();

    }
}
