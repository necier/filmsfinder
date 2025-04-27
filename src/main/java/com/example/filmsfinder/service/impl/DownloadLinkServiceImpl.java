package com.example.filmsfinder.service.impl;

import com.example.filmsfinder.domain.DownloadLink;
import com.example.filmsfinder.mapper.DownloadLinkMapper;
import com.example.filmsfinder.service.DownloadLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DownloadLinkServiceImpl implements DownloadLinkService {
    @Autowired private DownloadLinkMapper linkMapper;

    @Override
    public List<DownloadLink> getLinksByMovieId(Long movieId) {
        return linkMapper.selectByMovieId(movieId);
    }

    @Override
    public void addLink(DownloadLink link) {
        linkMapper.insert(link);
    }
}