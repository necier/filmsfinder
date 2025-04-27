package com.example.filmsfinder.service.impl;

import com.example.filmsfinder.domain.User;
import com.example.filmsfinder.mapper.UserMapper;
import com.example.filmsfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public void register(User user) {
        // 对明文密码进行 BCrypt 加密
        String raw = user.getPassword();
        String encoded = passwordEncoder.encode(raw);
        user.setPassword(encoded);
        userMapper.insert(user);
    }
}
