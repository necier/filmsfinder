package com.example.filmsfinder.domain;

import lombok.Data;
import java.time.Year;

@Data
public class Movie {
    private Long id;
    private String name;
    private String chineseName;
    private Year releaseYear;
    private String type;       // SHORT_FILM, DOCUMENTARY, TV_SERIES
    private String language;
    private Integer duration;  // 片长（分钟）
    private String posterUrl;
    private String description;
    private String imdbLink;
}