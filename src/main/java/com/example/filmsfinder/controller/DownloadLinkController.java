package com.example.filmsfinder.controller;

import com.example.filmsfinder.domain.DownloadLink;
import com.example.filmsfinder.domain.User;
import com.example.filmsfinder.service.DownloadLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/movies/{movieId}/downloads")
public class DownloadLinkController {

    @Autowired
    private DownloadLinkService linkService;

    /** 列出某部电影的所有下载链接 */
    @GetMapping
    public List<DownloadLink> listLinks(@PathVariable Long movieId) {
        return linkService.getLinksByMovieId(movieId);
    }

    /** 管理员添加下载链接 */
    @PostMapping
    public ResponseEntity<String> addLink(
            @PathVariable Long movieId,
            @RequestBody DownloadLink link,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("权限不足");
        }

        link.setMovieId(movieId);
        linkService.addLink(link);
        return ResponseEntity.ok("添加成功");
    }
}
