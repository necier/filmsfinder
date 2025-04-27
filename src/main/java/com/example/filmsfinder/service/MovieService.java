package com.example.filmsfinder.service;

import com.example.filmsfinder.domain.Movie;

import java.util.List;

public interface MovieService {
    Movie getMovieDetail(Long id);
    List<Movie> listAllMovies();
    void addMovie(Movie movie);
    void updateMovie(Movie movie);

    // 新增分页与搜索方法
    List<Movie> listMovies(int offset, int limit);
    List<Movie> searchMovies(String keyword, int offset, int limit);
    int countAllMovies();
    int countSearchMovies(String keyword);
}
