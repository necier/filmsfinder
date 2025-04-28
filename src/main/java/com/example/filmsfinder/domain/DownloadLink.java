package com.example.filmsfinder.domain;

import lombok.Data;

@Data
public class DownloadLink {
    private Long id;
    private Long movieId;
    private String url;
    private String accessLevel; // "VISITOR","USER","ADMIN"

    // 新增字段
    private String category;    // "MAGNET","HTTP"
    private String fileName;    // 例如 "SomeMovie.mkv"
    private Long fileSize;      // 字节数
    private String resolution;  // "720p","1080p","2160p","UNKNOWN"
}
