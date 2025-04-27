package com.example.filmsfinder.controller;

import com.example.filmsfinder.domain.User;
import com.example.filmsfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().length() < 5 || user.getUsername().length() > 20) {
            return "用户名长度需在5~20之间";
        }
        if (!user.getPassword().matches("^[A-Za-z0-9]{5,20}$")) {
            return "密码格式不合法";
        }
        if (userService.findByUsername(user.getUsername()) != null) {
            return "用户名已存在";
        }
        user.setUserType("USER");
        userService.register(user);
        return "注册成功";
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginUser, HttpSession session) {
        User user = userService.findByUsername(loginUser.getUsername());
        // 使用 BCrypt 匹配
        if (user == null || !passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("用户名或密码错误");
        }
        session.setAttribute("user", user);
        return ResponseEntity.ok("登录成功");
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "退出成功";
    }

    @GetMapping("/me")
    public ResponseEntity<User> me(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(user);
    }
}
