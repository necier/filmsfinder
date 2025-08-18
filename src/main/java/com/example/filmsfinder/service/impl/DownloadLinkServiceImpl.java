package com.example.filmsfinder.service.impl;

import com.example.filmsfinder.domain.DownloadLink;
import com.example.filmsfinder.mapper.DownloadLinkMapper;
import com.example.filmsfinder.service.DownloadLinkService;
import com.example.filmsfinder.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DownloadLinkServiceImpl implements DownloadLinkService {

    private final DownloadLinkMapper linkMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    DownloadLinkServiceImpl(DownloadLinkMapper linkMapper, StringRedisTemplate stringRedisTemplate) {
        this.linkMapper = linkMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 列出某部电影的所有下载链接
     */
    @Override
    public List<DownloadLink> getLinksByMovieId(Long movieId) {
        //先搜索缓存
        String redisKey = "movie:downloadlinks:" + movieId;
        Set<String> downloadLinkJson = stringRedisTemplate.opsForZSet()
                .range(redisKey, 0, -1);
        List<DownloadLink> res = new ArrayList<DownloadLink>();
        if (downloadLinkJson != null && !downloadLinkJson.isEmpty()) {
            // 反序列化成 List<DownloadLink>
            for (String downloadLink : downloadLinkJson) {
                DownloadLink temp = JsonUtils.toBean(downloadLink, DownloadLink.class);
                res.add(temp);
            }
            return res;
        }
        //未命中缓存，查找数据库
        res = linkMapper.selectByMovieId(movieId);
        //写入缓存
        for (DownloadLink downloadLink : res) {
            double score = downloadLink.getId();
            stringRedisTemplate.opsForZSet().add(redisKey, JsonUtils.toJson(downloadLink), score);
        }
        return res;
    }

    /**
     * 通过linkId找到对应的link
     */
    @Override
    public DownloadLink getLinkByLinkId(Long linkId) {
        String redisKey = "downloadlink:" + linkId;
        var linkJson = stringRedisTemplate.opsForValue().get(redisKey);
        DownloadLink downloadLink;
        if (linkJson != null) {
            downloadLink = JsonUtils.toBean(linkJson, DownloadLink.class);
            return downloadLink;
        }
        //未命中缓存
        downloadLink = linkMapper.selectByLinkId(linkId);
        //存入缓存
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJson(downloadLink));
        return downloadLink;
    }


    /**
     * 添加下载链接：
     * - 优先使用管理员填写的 category、fileSize、resolution
     * - Magnet 链接：若管理员未填写，则从 URL 参数或文件名中解析
     * - HTTP 链接：若管理员未填写，则设置默认 UNKNOWN
     */
    @Override
    public void addLink(DownloadLink link) {
        String url = link.getUrl();

        // 管理员填写的优先值
        String adminCategory = link.getCategory();
        Long adminFileSize = link.getFileSize();
        String adminResolution = link.getResolution();

        if (url.startsWith("magnet:")) {
            // category
            if (adminCategory == null || adminCategory.isEmpty()) {
                link.setCategory("MAGNET");
            }

            // 解析文件名（dn 参数）
            Map<String, String> params = parseQueryParams(url);
            String dn = params.get("dn");
            link.setFileName(dn);

            // resolution
            if (adminResolution != null && !adminResolution.isEmpty()) {
                link.setResolution(adminResolution);
            } else {
                link.setResolution(parseResolution(dn));
            }

            // fileSize
            link.setFileSize(adminFileSize);

        } else {
            // HTTP 或其他
            if (adminCategory == null || adminCategory.isEmpty()) {
                link.setCategory("HTTP");
            }
            // resolution
            if (adminResolution != null && !adminResolution.isEmpty()) {
                link.setResolution(adminResolution);
            } else {
                link.setResolution("UNKNOWN");
            }
            // fileSize
            link.setFileSize(adminFileSize);
            // fileName 保持管理员未填写时为空
            if (link.getFileName() == null) {
                link.setFileName(null);
            }
        }
        //先操作数据库
        linkMapper.insert(link);
        //删除缓存
        String redisKey = "movie:downloadlinks:" + link.getMovieId();
        stringRedisTemplate.delete(redisKey);
    }

    /**
     * 删除下载链接
     */
    @Override
    public void deleteLinkById(Long movieId, Long linkId) {
        //先操作数据库
        linkMapper.deleteById(linkId);
        //删除该电影的下载链接缓存
        String redisKey = "movie:downloadlinks:" + movieId;
        stringRedisTemplate.delete(redisKey);
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
