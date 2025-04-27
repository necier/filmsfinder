package com.example.filmsfinder.mapper;

import com.example.filmsfinder.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectByMovieId(@Param("movieId") Long movieId);
    void insert(Comment comment);
}