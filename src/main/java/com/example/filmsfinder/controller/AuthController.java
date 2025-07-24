package com.example.filmsfinder.controller;

import com.example.filmsfinder.domain.User;
import com.example.filmsfinder.filter.JwtAuthenticationFilter;
import com.example.filmsfinder.service.UserService;
import com.example.filmsfinder.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    AuthController(
            AuthenticationManager authenticationManager,
            UserService userService,
            JwtUtils jwtUtils,
            StringRedisTemplate redisTemplate
    ) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().length() < 4 || user.getUsername().length() > 20) {
            return ResponseEntity.badRequest().body("用户名长度需在4~20之间");
        }
        if(!user.getUsername().matches("^[A-Za-z0-9]{4,20}$")) {
            return ResponseEntity.badRequest().body("用户名不符合规范");
        }
        if (!user.getPassword().matches("^[A-Za-z0-9]{5,20}$")) {
            return ResponseEntity.badRequest().body("密码格式不合法");
        }
        if (userService.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("用户名已经存在");
        }
        user.setUserType("USER");
        userService.register(user);
        return ResponseEntity.ok("注册成功");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        try {
            // 1. 使用 AuthenticationManager 进行认证，它会自动调用 UserDetailsService
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword())
            );

            // 2. 认证成功后，将认证信息存入 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. 从数据库获取完整的 User 对象以生成 Token
            User user = userService.findByUsername(loginUser.getUsername());

            // 4. 生成 JWT
            final String token = jwtUtils.generateToken(user);

            // 5. 将 JWT 封装在 Map 中返回给前端
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        // 从 "Bearer <token>" 中提取 token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            // 1. 从 token 中解析出过期时间
            Date expiration = jwtUtils.getExpirationDateFromToken(jwt);
            long remainingMillis = expiration.getTime() - System.currentTimeMillis();

            if (remainingMillis > 0) {
                // 2. 将 token 存入 Redis 黑名单，过期时间设置为 token 的剩余有效时间
                redisTemplate.opsForValue().set(
                        JwtAuthenticationFilter.TOKEN_BLACKLIST_PREFIX + jwt,
                        "1",
                        remainingMillis,
                        TimeUnit.MILLISECONDS
                );
            }
        }
        return ResponseEntity.ok("退出成功");
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        // 1. 从 SecurityContext 中获取当前认证的用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. principal 就是 loadUserByUsername 返回的 UserDetails 对象
        org.springframework.security.core.userdetails.User springUser = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        User user = userService.findByUsername(springUser.getUsername());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 3. 返回安全的、不含密码的用户信息
        Map<String, Object> userSafeData = new HashMap<>();
        userSafeData.put("id", user.getId());
        userSafeData.put("username", user.getUsername());
        userSafeData.put("userType", user.getUserType());
        return ResponseEntity.ok(userSafeData);
    }
}