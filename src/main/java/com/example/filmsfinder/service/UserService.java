package com.example.filmsfinder.service;

import com.example.filmsfinder.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User findByUsername(String username);
    void register(User user);

    // loadUserByUsername(String username) 方法会由 UserDetailsService 接口带来
}