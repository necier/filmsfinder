package com.example.filmsfinder.service.impl;

import com.example.filmsfinder.domain.Comment;
import com.example.filmsfinder.mapper.CommentMapper;
import com.example.filmsfinder.service.CommentService;
import com.example.filmsfinder.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CommentServiceImpl implements CommentService {

    //为了效率而存储重复的数据
    //把数据存到movie:comments:{movieId} 以及 comment:{commentId}

    private final StringRedisTemplate stringRedisTemplate;
    private final CommentMapper commentMapper;

    @Autowired
    CommentServiceImpl(CommentMapper commentMapper, StringRedisTemplate stringRedisTemplate) {
        this.commentMapper = commentMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public List<Comment> getComments(Long movieId) {
        //使用ZSet保存以保证时间排序
        String redisKey = "movie:comments:" + movieId;
        Set<String> commentsJson = stringRedisTemplate.opsForZSet()
                .reverseRange(redisKey, 0, -1);
        List<Comment> res = new ArrayList<>();
        if (commentsJson != null && !commentsJson.isEmpty()) {
            // 反序列化成 List<Comment>
            for (String commentJson : commentsJson) {
                Comment temp = JsonUtils.toBean(commentJson, Comment.class);
                res.add(temp);
            }
            return res;
        }
        //未命中缓存,找数据库
        res = commentMapper.selectByMovieId(movieId);
        //写入缓存
        for (Comment comment : res) {
            //将LocalDateTime转成时间戳再转成score
            double score = comment.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            stringRedisTemplate.opsForZSet().add(redisKey, JsonUtils.toJson(comment), score);
        }
        //返回List<Comment>
        return res;

    }

    //先更新数据库 + 再删除缓存
    @Override
    @Transactional
    public void addComment(Comment comment) {
        //操作数据库
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        //删除缓存
        String redisKey = "movie:comments:" + comment.getMovieId();
        stringRedisTemplate.delete(redisKey);
    }

    @Override
    @Transactional
    public void deleteComment(Comment comment) {
        //操作数据库
        commentMapper.delete(comment);
        //删除缓存
        String redisKey = "movie:comments:" + comment.getMovieId();
        stringRedisTemplate.delete(redisKey);
    }

}