package com.example.filmsfinder.controller;

import com.example.filmsfinder.domain.Comment;
import com.example.filmsfinder.domain.User;
import com.example.filmsfinder.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/movies/{movieId}/comments")
public class CommentController {
    @Autowired private CommentService commentService;

    @GetMapping
    public List<Comment> list(@PathVariable Long movieId) {
        return commentService.getComments(movieId);
    }

    @PostMapping
    public void post(
            @PathVariable Long movieId,
            @RequestBody Comment comment,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        comment.setMovieId(movieId);
        comment.setUserId(user.getId());
        commentService.addComment(comment);
    }
}