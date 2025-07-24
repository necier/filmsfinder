package com.example.filmsfinder.service.impl;

import com.example.filmsfinder.domain.Movie;
import com.example.filmsfinder.mapper.MovieMapper;
import com.example.filmsfinder.service.MovieService;
import com.example.filmsfinder.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class MovieServiceImpl implements MovieService {


    private final MovieMapper movieMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    MovieServiceImpl(MovieMapper movieMapper, StringRedisTemplate stringRedisTemplate) {
        this.movieMapper = movieMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Movie getMovieDetail(Long id) {
        String redisKey = "movie:" + id;
        String movieJson = stringRedisTemplate.opsForValue().get(redisKey);
        Movie movie;
        if (movieJson != null) {
            movie = JsonUtils.toBean(movieJson, Movie.class);
            return movie;
        }
        //未命中缓存
        movie = movieMapper.selectById(id);
        //写入缓存
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJson(movie));
        return movie;
    }

    @Override
    public List<Movie> listAllMovies() {
        String redisKey = "movie:all";
        List<Movie> movieList = new ArrayList<Movie>();
        Set<String> movieJson = stringRedisTemplate.opsForZSet().range(redisKey, 0, -1);
        if (movieJson != null && !movieJson.isEmpty()) {
            for (String movie : movieJson) {
                movieList.add(JsonUtils.toBean(movie, Movie.class));
            }
            return movieList;
        }
        //未命中缓存
        movieList = movieMapper.selectAll();
        //写入缓存
        for (Movie movie : movieList) {
            double score = movie.getId();
            stringRedisTemplate.opsForZSet().add(redisKey, JsonUtils.toJson(movie), score);
        }
        return movieList;
    }

    @Override
    public void addMovie(Movie movie) {

        movieMapper.insert(movie);
        //更新缓存
        long id = movie.getId();
        String redisKey = "movie:" + id;
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJson(movie));
        //更新movie:all缓存
        stringRedisTemplate.opsForZSet().add(redisKey, JsonUtils.toJson(movie), movie.getId());
    }

    @Override
    public void updateMovie(Movie movie) {

        movieMapper.update(movie);
        //更新缓存
        long id = movie.getId();
        String redisKey = "movie:" + id;
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJson(movie));
        //删除movie:all缓存
        stringRedisTemplate.delete("movie:all");
    }

    // ===== 新增分页与搜索实现 =====

    @Override
    public List<Movie> listMovies(int offset, int limit) {
        //从movie:all中筛选
        String redisKey = "movie:all";
        List<Movie> res = new ArrayList<>();
        Set<String> moviesJson = stringRedisTemplate.opsForZSet()
                .range(redisKey, offset, offset + limit - 1);
        if (moviesJson != null && !moviesJson.isEmpty()) {
            for (String movie : moviesJson) {
                res.add(JsonUtils.toBean(movie, Movie.class));
            }
            return res;
        }
        //未命中缓存
        res = movieMapper.selectPaged(offset, limit);
        //写入缓存
        for (Movie movie : res) {
            String movieRedisKey = "movie:" + movie.getId();
            String movieJson = JsonUtils.toJson(movie);
            //设置movie:{id}
            stringRedisTemplate.opsForValue().set(movieRedisKey, movieJson);
            //设置movie:all,ZSet自动去重
            stringRedisTemplate.opsForZSet().add(redisKey, movieJson, movie.getId());
        }
        return res;
    }

    @Override
    public List<Movie> searchMovies(String keyword, int offset, int limit) {
        return movieMapper.searchPaged(keyword, offset, limit);
    }

    @Override
    public int countAllMovies() {

        return movieMapper.countAll();
    }

    @Override
    public int countSearchMovies(String keyword) {

        return movieMapper.countSearch(keyword);
    }
}
