package com.example.filmsfinder.mapper;

import com.example.filmsfinder.domain.DownloadLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DownloadLinkMapper {
    List<DownloadLink> selectByMovieId(@Param("movieId") Long movieId);
    DownloadLink selectByLinkId(Long linkId);
    void insert(DownloadLink link);
    void deleteById(@Param("id") Long id);
}