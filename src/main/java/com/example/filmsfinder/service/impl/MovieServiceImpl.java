package com.example.filmsfinder.service.impl;

import com.example.filmsfinder.domain.Movie;
import com.example.filmsfinder.mapper.MovieMapper;
import com.example.filmsfinder.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MovieMapper movieMapper;

    @Override
    public Movie getMovieDetail(Long id) {
        return movieMapper.selectById(id);
    }

    @Override
    public List<Movie> listAllMovies() {
        return movieMapper.selectAll();
    }

    @Override
    public void addMovie(Movie movie) {
        movieMapper.insert(movie);
    }

    @Override
    public void updateMovie(Movie movie) {
        movieMapper.update(movie);
    }

    // ===== 新增分页与搜索实现 =====

    @Override
    public List<Movie> listMovies(int offset, int limit) {
        return movieMapper.selectPaged(offset, limit);
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
