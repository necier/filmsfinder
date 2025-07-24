package com.example.filmsfinder.controller;

import com.example.filmsfinder.domain.Comment;
import com.example.filmsfinder.domain.User;
import com.example.filmsfinder.service.CommentService;
import com.example.filmsfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/movies/{movieId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @GetMapping
    public List<Comment> list(@PathVariable Long movieId) {
        return commentService.getComments(movieId);
    }

    @PostMapping
    public void post(
            @PathVariable Long movieId,
            @RequestBody Comment comment
    ) {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        comment.setMovieId(movieId);
        comment.setUserId(user.getId());
        commentService.addComment(comment);
    }

    @DeleteMapping
    public void delete(@RequestBody Comment comment) {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        //评论的发布者或者管理员有权删评
        if (!Objects.equals(user.getId(), comment.getUserId()) && !Objects.equals(user.getUserType(), "ADMIN")) {
            throw new RuntimeException("无权进行该操作");
        }
        commentService.deleteComment(comment);
    }

    /**
     * 获取当前登录用户（非匿名用户），根据 username 从数据库中查完整 User 对象。
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
        return userService.findByUsername(username);
    }
}