package com.example.filmsfinder.mapper;

import com.example.filmsfinder.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    void insert(User user);
}