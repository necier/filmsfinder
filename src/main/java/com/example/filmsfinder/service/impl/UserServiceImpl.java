package com.example.filmsfinder.service.impl;

import com.example.filmsfinder.domain.User;
import com.example.filmsfinder.mapper.UserMapper;
import com.example.filmsfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public void register(User user) {
        String raw = user.getPassword();
        String encoded = passwordEncoder.encode(raw);
        user.setPassword(encoded);
        userMapper.insert(user);
    }

    /**
     * 实现 UserDetailsService 的核心方法
     * Spring Security 在进行身份验证时会自动调用此方法
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 从数据库中根据用户名查询用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // 2. 将User对象转换成 Spring Security 需要的 UserDetails 对象
        //  user.getUsername(): 用户名
        //  user.getPassword(): 已加密的密码
        //  Collections.singletonList(...): 权限列表
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getUserType()))
        );
    }
}