package com.example.filmsfinder.service.impl;

import com.example.filmsfinder.domain.Comment;
import com.example.filmsfinder.mapper.CommentMapper;
import com.example.filmsfinder.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired private CommentMapper commentMapper;

    @Override
    public List<Comment> getComments(Long movieId) {
        return commentMapper.selectByMovieId(movieId);
    }

    @Override
    public void addComment(Comment comment) {
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
    }
}
