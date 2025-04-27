package com.example.filmsfinder.mapper;

import com.example.filmsfinder.domain.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MovieMapper {
    // 原有
    List<Movie> selectAll();
    Movie selectById(@Param("id") Long id);
    void insert(Movie movie);
    void update(Movie movie);

    // 分页查询：首页
    List<Movie> selectPaged(@Param("offset") int offset, @Param("limit") int limit);
    // 关键词模糊查询＋分页
    List<Movie> searchPaged(@Param("keyword") String keyword,
                            @Param("offset") int offset,
                            @Param("limit") int limit);
    // 总数统计
    int countAll();
    int countSearch(@Param("keyword") String keyword);
}
