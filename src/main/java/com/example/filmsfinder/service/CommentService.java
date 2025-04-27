package com.example.filmsfinder.service;

import com.example.filmsfinder.domain.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> getComments(Long movieId);
    void addComment(Comment comment);
}