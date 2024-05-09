package com.web.service;

import com.web.repo.User;

public interface UserService {
    User loginCheckByIdPw(String id, String pw);
    void signup(User usr);
    User findUserByEmail(String email);
}
