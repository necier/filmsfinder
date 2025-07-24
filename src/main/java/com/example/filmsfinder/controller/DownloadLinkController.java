package com.example.filmsfinder.controller;

import com.example.filmsfinder.domain.DownloadLink;
import com.example.filmsfinder.service.DownloadLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/api/movies/{movieId}/downloadlinks")
public class DownloadLinkController {


    private final DownloadLinkService linkService;

    @Autowired
    DownloadLinkController(DownloadLinkService linkService) {

        this.linkService = linkService;
    }

    /**
     * 列出某部电影的所有下载链接
     */
    @GetMapping
    public List<DownloadLink> listLinks(@PathVariable Long movieId) {

        return linkService.getLinksByMovieId(movieId);
    }

    @GetMapping("/{linkId}")
    public DownloadLink getLinkById(
            @PathVariable Long movieId,
            @PathVariable Long linkId) {
        return linkService.getLinkByLinkId(linkId);
    }

    /**
     * 管理员添加下载链接
     */
    @PostMapping
    public ResponseEntity<String> addLink(
            @PathVariable Long movieId,
            @RequestBody DownloadLink link) {

        //仅ADMIN可操作
        link.setMovieId(movieId);
        linkService.addLink(link);
        return ResponseEntity.ok("添加成功");
    }

    /**
     * 管理员删除下载链接
     */
    @DeleteMapping("/{linkId}")
    public ResponseEntity<Void> deleteLink(
            @PathVariable Long movieId,
            @PathVariable Long linkId) {

        //仅ADMIN可操作
        linkService.deleteLinkById(linkId);
        return ResponseEntity.noContent().build();
    }


}
