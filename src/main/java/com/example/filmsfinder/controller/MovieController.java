package com.example.filmsfinder.controller;

import com.example.filmsfinder.domain.Movie;
import com.example.filmsfinder.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    @Autowired
    MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * 分页 + 关键词搜索
     * GET /api/movies?page=1&keyword=关键字
     */
    @GetMapping
    public Map<String, Object> listAll(
            // 分页查询
            @RequestParam(defaultValue = "1") int page,
            // 模糊查询
            @RequestParam(required = false) String keyword) {

        //一页24张卡片
        int pageSize = 24;
        int offset = (page - 1) * pageSize;

        List<Movie> movies;
        int total;

        if (StringUtils.hasText(keyword)) {
            movies = movieService.searchMovies(keyword, offset, pageSize);
            total = movieService.countSearchMovies(keyword);
        } else {
            movies = movieService.listMovies(offset, pageSize);
            total = movieService.countAllMovies();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("movies", movies);
        result.put("total", total);
        result.put("page", page);
        result.put("size", pageSize);
        return result;
    }

    @GetMapping("/{id}")
    public Movie getDetail(@PathVariable Long id) {
        //查找缓存

        return movieService.getMovieDetail(id);
    }

    @PostMapping
    public void addMovie(@RequestBody Movie movie) {

        //仅ADMIN可操作
        movieService.addMovie(movie);
    }

    @PutMapping("/{id}")
    public void updateMovie(@PathVariable Long id, @RequestBody Movie movie) {

        //仅ADMIN可操作
        movie.setId(id);
        movieService.updateMovie(movie);
    }
}
