package com.example.filmsfinder.service;

import com.example.filmsfinder.domain.User;

public interface UserService {
    User findByUsername(String username);
    void register(User user);
}