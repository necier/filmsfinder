package com.example.filmsfinder.domain;

import lombok.Data;

@Data
public class DownloadLink {
    private Long id;
    private Long movieId;
    private String url;
    private String accessLevel; // "VISITOR","USER","ADMIN"
}