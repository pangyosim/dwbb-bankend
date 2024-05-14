package com.web.controller;

import com.web.service.MailService;
import com.web.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import com.web.repo.*;
@CrossOrigin("https://www.dwbb.online/")
@RestController
@PropertySource("classpath:application.properties")
public class UserController {

    @Value("${user.jwtkey}")
    private String securityKey;
    private final Long expiredTime = 1000 * 60L * 60L * 3L;
    private final UserService us;
    private final MailService ms;
    @Autowired
    public UserController(UserService us,MailService ms) {
        this.us = us;
        this.ms = ms;
    }

    @PostMapping("/check-login")
    @CrossOrigin
    public String[] loginMethod (@RequestBody User usr){
        try {
            Date now = new Date();
            User login_result = us.loginCheckByIdPw(usr.getId(),usr.getPw());
            System.out.println(login_result);
            if( login_result != null) {
                String[] res_arr = new String[3];
                res_arr[0] = login_result.getNickname();
                res_arr[1] = login_result.getRole();
                res_arr[2] = Jwts.builder()
                                .setSubject(login_result.getNickname())
                                .setHeader(createHeader())
                                .setClaims(createClaims(login_result))
                                .setExpiration(new Date(now.getTime()+expiredTime))
                                .signWith(SignatureAlgorithm.HS256,securityKey)
                                .compact();
                return res_arr;
            }
        } catch (Exception e) {
                e.fillInStackTrace();
        }
        return null;
    }
    @PostMapping("/signup")
    @CrossOrigin
    public String signup (@RequestBody User usr){
        us.signup(usr);
        return "signupsuccess";
    }

    @PostMapping("/check-email")
    @CrossOrigin
    public User checkemailMethod (@RequestBody User usr){
        try{
            User finduser = us.findUserByEmail(usr.getEmail());
            if(finduser != null) {
                int i = ms.sendMail(usr.getEmail());
                finduser.setSeq(Long.parseLong(Integer.toString(i)));
                return finduser;
            } else {
                User find_fail = new User();
                find_fail.setEmail("findfail");
                return find_fail;
            }
        } catch (Exception e){
            e.fillInStackTrace();
        }
        return null;
    }

    @PostMapping("/distinct-email")
    @CrossOrigin
    public String checkdistinctemail (@RequestBody User usr){
        try{
            User distinct_check = us.findUserByEmail(usr.getEmail());
            if( distinct_check != null){
                return "distinct";
            } else {
                return "available";
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return  "";
    }

    private Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("type", "JWT");
        header.put("alg", "HS256"); // 해시 256 사용하여 암호화
        header.put("regDate", System.currentTimeMillis());
        return header;
    }
    private Map<String, Object> createClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("pw", user.getPw()); // username
        claims.put("roles", user.getRole()); // 인가정보
        return claims;
    }
}
