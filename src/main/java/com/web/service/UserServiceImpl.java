package com.web.service;

import com.web.persistence.UserRepository;
import com.web.repo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepo;
    @Autowired
    public UserServiceImpl(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public User loginCheckByIdPw(String id, String pw) {
        try {
            return userRepo.loginCheckByIdPw(id,pw);
        } catch (Exception e){
            e.fillInStackTrace();
        }
        return null;
    }

    @Override
    public void signup(User usr) {
        userRepo.save(usr);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepo.findUserByEmail(email);
    }
}
