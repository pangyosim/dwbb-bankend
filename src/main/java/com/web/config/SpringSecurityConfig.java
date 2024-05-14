package com.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf().disable()
                .cors().and()
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers("/","/check-login","/signup","/check-email","/distinct-email",
                        "/notice-all","/notice-views","/notice-register","/notice-delete",
                        "/qna-all","/qna-views","/qna-register","/qna-delete","/qna-comments",
                        "/bank-data","/trans-bank-data",
                        "/park-data","/trans-park-data").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic();
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
