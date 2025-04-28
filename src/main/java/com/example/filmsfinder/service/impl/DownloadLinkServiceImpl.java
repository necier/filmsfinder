package com.example.filmsfinder.service.impl;

import com.example.filmsfinder.domain.DownloadLink;
import com.example.filmsfinder.mapper.DownloadLinkMapper;
import com.example.filmsfinder.service.DownloadLinkService;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DownloadLinkServiceImpl implements DownloadLinkService {
    @Autowired
    private DownloadLinkMapper linkMapper;

    @Override
    public List<DownloadLink> getLinksByMovieId(Long movieId) {
        return linkMapper.selectByMovieId(movieId);
    }

    @Override
    public void addLink(DownloadLink link) {
        String url = link.getUrl();
        if (url.startsWith("magnet:")) {
            link.setCategory("MAGNET");
            extractFromMagnet(link, url);
        } else {
            link.setCategory("HTTP");
            link.setResolution("UNKNOWN");
        }
        linkMapper.insert(link);
    }

    @Override
    public void deleteLinkById(Long linkId) {
        linkMapper.deleteById(linkId);
    }


//     使用 jlibtorrent.fetchMagnet 获取 .torrent 原始元数据，
//     再通过 TorrentInfo.bdecode 解析并用 FileStorage 提取属性
    private void extractFromMagnet(DownloadLink link, String magnetUrl) {
        SessionManager session = new SessionManager();
        session.start();
        try {
            // fetchMagnet(uri, timeout) -> bencoded metadata :contentReference[oaicite:0]{index=0}
            byte[] meta = session.fetchMagnet(magnetUrl, 60);
            if (meta == null) {
                throw new RuntimeException("无法获取元数据");
            }
            // 解析 metadata 为 TorrentInfo :contentReference[oaicite:1]{index=1}
            TorrentInfo ti = TorrentInfo.bdecode(meta);

            // 可选：保存 .torrent 文件
            try {
                File tmp = File.createTempFile("tf_", ".torrent");
                Files.write(tmp.toPath(), ti.bencode());
            } catch (IOException ignored) {}

            // 提取最大文件作为主文件，获取文件名和大小 :contentReference[oaicite:2]{index=2}
            FileStorage fs = ti.files();
            long maxSize = 0;
            String bestName = null;
            for (int i = 0; i < fs.numFiles(); i++) {
                long size = fs.fileSize(i);
                String path = fs.filePath(i);
                if (size > maxSize) {
                    maxSize = size;
                    bestName = path;
                }
            }

            link.setFileName(bestName);
            link.setFileSize(maxSize);
            link.setResolution(parseResolution(bestName));
        } finally {
            session.stop();
        }
    }

//    简单正则解析文件名中的分辨率，如 1080p，否则返回 UNKNOWN
//    后续需要更好的方法，先暂时用着
    private String parseResolution(String name) {
        if (name == null) return "UNKNOWN";
        Matcher m = Pattern.compile("(\\d{3,4}p)").matcher(name);
        return m.find() ? m.group(1) : "UNKNOWN";
    }
}
