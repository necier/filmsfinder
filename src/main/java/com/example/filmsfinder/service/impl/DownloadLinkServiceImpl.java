package com.example.filmsfinder.service.impl;

import com.example.filmsfinder.domain.DownloadLink;
import com.example.filmsfinder.mapper.DownloadLinkMapper;
import com.example.filmsfinder.service.DownloadLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DownloadLinkServiceImpl implements DownloadLinkService {

    @Autowired
    private DownloadLinkMapper linkMapper;

    /**
     * 列出某部电影的所有下载链接
     */
    @Override
    public List<DownloadLink> getLinksByMovieId(Long movieId) {
        return linkMapper.selectByMovieId(movieId);
    }

    /**
     * 添加下载链接：
     * - Magnet 链接：从 URL 参数中解析 fileName（dn）和分辨率（文件名中的 xxxp）
     * - HTTP 链接：默认 UNKNOWN
     */
    @Override
    public void addLink(DownloadLink link) {
        String url = link.getUrl();
        if (url.startsWith("magnet:")) {
            link.setCategory("MAGNET");
            Map<String, String> params = parseQueryParams(url);
            // 从 dn 参数获取文件名
            String dn = params.get("dn");
            link.setFileName(dn);
            // 文件大小无法通过 Magnet 链接获取，留空或默认
            link.setFileSize(null);
            // 从文件名解析 xxxp
            link.setResolution(parseResolution(dn));
        } else {
            link.setCategory("HTTP");
            link.setFileName(null);
            link.setFileSize(null);
            link.setResolution("UNKNOWN");
        }
        linkMapper.insert(link);
    }

    /**
     * 删除下载链接
     */
    @Override
    public void deleteLinkById(Long linkId) {
        linkMapper.deleteById(linkId);
    }

    /**
     * 简单解析 URL 中的查询参数
     */
    private Map<String, String> parseQueryParams(String url) {
        Map<String, String> params = new HashMap<>();
        int idx = url.indexOf('?');
        if (idx < 0) {
            return params;
        }
        String query = url.substring(idx + 1);
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq > 0 && eq < pair.length() - 1) {
                String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    /**
     * 从文件名中提取分辨率（例如 "1080p"）
     */
    private String parseResolution(String name) {
        if (name == null) {
            return "UNKNOWN";
        }
        Matcher m = Pattern.compile("(\\d{3,4}p)").matcher(name);
        return m.find() ? m.group(1) : "UNKNOWN";
    }
}
