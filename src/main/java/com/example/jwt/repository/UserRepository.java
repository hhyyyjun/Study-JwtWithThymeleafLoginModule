package com.example.jwt.repository;

import com.example.jwt.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
* @package : com.example.jwt.repository
* @name : UserRepository.java
* @date : 2023-04-19 오후 5:24
* @author : hj
* @Description: User Entity에 매핑을 위해 생성
 *              UserService에서 호출
**/

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = "authorities") //쿼리가 수행될 때 Eager조회로 authorities정보를 같이 가져온다
    Optional<User> findOneWithAuthoritiesByUsername(String username);
    //username을 기준으로 user정보를 조회하며 권한 정보를 같이 가져옴
}