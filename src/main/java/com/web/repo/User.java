package com.web.repo;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "dwbbmember")
@SequenceGenerator(
        name = "MEMBER_SEQ_GENERATOR",
        sequenceName = "DWBBUSER_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_SEQ_GENERATOR")
    private Long seq;
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "pw", nullable = false)
    private String pw;
    @Column(name = "nickname", nullable = false)
    private String nickname;
    @Column(name= "phone", nullable = false, length = 255)
    private String phone;
    @Column(name= "email", nullable = false, length = 255)
    private String email;
    @Column(name= "role", nullable = false)
    private String role;
}
