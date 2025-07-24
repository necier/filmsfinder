package com.example.filmsfinder.service;

import com.example.filmsfinder.domain.DownloadLink;
import java.util.List;

public interface DownloadLinkService {
    List<DownloadLink> getLinksByMovieId(Long movieId);
    DownloadLink getLinkByLinkId(Long linkId);
    void addLink(DownloadLink link);
    void deleteLinkById(Long linkID);

}