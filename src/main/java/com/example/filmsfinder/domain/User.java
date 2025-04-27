package com.example.filmsfinder.domain;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String userType; // "USER" 或 "ADMIN"
}