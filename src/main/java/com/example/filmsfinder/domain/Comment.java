package com.example.filmsfinder.domain;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class Comment {
    private Long id;
    private Long movieId;    // 关联 Movie.id
    private Long userId;     // 关联用户（可扩展）
    private Integer rating;  // 打分（1–5）
    private String content;  // 评论内容
    private LocalDateTime createdAt;
    // getters/setters...
}
