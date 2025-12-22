package com.hiphop.admin.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CrawlerService {
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * 爬取内容：支持微信公众号文章、B站视频、网易云音乐歌曲/专辑、得物商品详情
     * @param url 文章链接、BV号、网易云链接或得物商品详情链接
     * @return 包含 title, coverImage, sourceUrl 的 Map
     */
    public Map<String, String> fetchContent(String url) throws IOException, InterruptedException {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url 不能为空");
        }
        // B站 BV
        if (url.startsWith("BV") || url.startsWith("bv") || url.startsWith("BV1") || url.startsWith("bv1")) {
            return fetchBilibili(url);
        }
        // 微信公众号
        if (url.contains("mp.weixin.qq.com") || url.contains("weixin.qq.com")) {
            return fetchWeChat(url);
        }
        // 网易云音乐（歌曲/专辑）
        if (url.contains("music.163.com")) {
            return fetchNeteaseMusic(url);
        }
        // 得物商品详情
        if (url.contains("dewu.com/product-detail")) {
            return fetchDewuProduct(url);
        }
        throw new IllegalArgumentException("不支持的链接类型，仅支持：微信公众号、B站视频、网易云音乐、得物商品详情页");
    }

    /**
     * 搜索网易云音乐歌曲
     *
     * @param keyword 关键词
     * @param limit   返回数量（1-30）
     */
    public List<Map<String, Object>> searchNeteaseSongs(String keyword, Integer limit) throws IOException, InterruptedException {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("关键词不能为空");
        }
        int realLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 30));
        String encodedKeyword = URLEncoder.encode(keyword.trim(), java.nio.charset.StandardCharsets.UTF_8);
        String apiUrl = "https://music.163.com/api/search/get/web?csrf_token=&hlpretag=&hlposttag=&s="
                + encodedKeyword + "&type=1&offset=0&total=true&limit=" + realLimit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://music.163.com/")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("网易云搜索失败，状态码: " + response.statusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode songs = root.path("result").path("songs");
        List<Map<String, Object>> result = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        if (songs.isArray()) {
            for (JsonNode songNode : songs) {
                long id = songNode.path("id").asLong();
                if (id == 0) continue;
                ids.add(id);

                String title = songNode.path("name").asText("");
                String artist = extractArtist(songNode);
                String cover = extractCover(songNode);
                String link = "https://music.163.com/song?id=" + id;

                Map<String, Object> item = new HashMap<>();
                item.put("id", id);
                item.put("title", title);
                item.put("artist", artist);
                item.put("cover", cover);
                item.put("linkUrl", link);
                result.add(item);
            }
        }

        // 进一步用歌曲详情接口补全/纠正封面与艺人
        if (!ids.isEmpty()) {
            Map<Long, Map<String, String>> detailMap = fetchSongDetails(ids);
            for (Map<String, Object> item : result) {
                Object idObj = item.get("id");
                if (!(idObj instanceof Number)) continue;
                long id = ((Number) idObj).longValue();
                Map<String, String> detail = detailMap.get(id);
                if (detail != null) {
                    String detailCover = detail.get("cover");
                    String detailArtist = detail.get("artist");
                    if (detailCover != null && !detailCover.isEmpty()) {
                        item.put("cover", detailCover);
                    }
                    if (detailArtist != null && !detailArtist.isEmpty()) {
                        item.put("artist", detailArtist);
                    }
                }
                // 兜底：如果仍然没有封面，抓取歌曲页面 meta og:image
                Object coverObj = item.get("cover");
                if (coverObj == null || String.valueOf(coverObj).isBlank()) {
                    String pageCover = fetchSongCoverFromPage(id);
                    if (pageCover != null && !pageCover.isEmpty()) {
                        item.put("cover", pageCover);
                    }
                }
                // 最终兜底：仍为空则使用统一占位图，避免前端出现随机图或空白
                Object finalCover = item.get("cover");
                if (finalCover == null || String.valueOf(finalCover).isBlank()) {
                    item.put("cover", "https://picsum.photos/800/800?random=netease_placeholder");
                }
            }
        }
        return result;
    }

    /**
     * 搜索网易云音乐专辑
     *
     * @param keyword 关键词
     * @param limit   返回数量（1-30）
     */
    public List<Map<String, Object>> searchNeteaseAlbums(String keyword, Integer limit) throws IOException, InterruptedException {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("关键词不能为空");
        }
        int realLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 30));
        String encodedKeyword = URLEncoder.encode(keyword.trim(), java.nio.charset.StandardCharsets.UTF_8);
        // type=10 为专辑搜索
        String apiUrl = "https://music.163.com/api/search/get/web?csrf_token=&hlpretag=&hlposttag=&s="
                + encodedKeyword + "&type=10&offset=0&total=true&limit=" + realLimit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://music.163.com/")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("网易云专辑搜索失败，状态码: " + response.statusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode albums = root.path("result").path("albums");
        List<Map<String, Object>> result = new ArrayList<>();
        if (albums.isArray()) {
            for (JsonNode albumNode : albums) {
                long id = albumNode.path("id").asLong();
                if (id == 0) continue;
                String name = albumNode.path("name").asText("");
                // 处理多个艺人：优先从 artists 数组拼接所有艺人名
                String artistName = "";
                JsonNode artistsNode = albumNode.path("artists");
                if (artistsNode != null && artistsNode.isArray() && artistsNode.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode a : artistsNode) {
                        String n = a.path("name").asText("");
                        if (n != null && !n.isEmpty()) {
                            if (sb.length() > 0) {
                                sb.append(" / ");
                            }
                            sb.append(n);
                        }
                    }
                    artistName = sb.toString();
                }
                // 如果没有 artists 数组，则退回单个 artist 字段
                if ((artistName == null || artistName.isEmpty())) {
                    JsonNode artistNode = albumNode.path("artist");
                    if (artistNode != null && !artistNode.isMissingNode()) {
                        artistName = artistNode.path("name").asText("");
                    }
                }
                String cover = albumNode.path("picUrl").asText("");
                if (cover != null && !cover.isEmpty()) {
                    if (cover.startsWith("//")) {
                        cover = "https:" + cover;
                    } else if (cover.startsWith("http://")) {
                        cover = cover.replace("http://", "https://");
                    }
                    if (!cover.contains("?param=")) {
                        cover = cover + "?param=400y400";
                    }
                } else {
                    cover = "https://picsum.photos/800/800?random=netease_album_placeholder";
                }
                String link = "https://music.163.com/album?id=" + id;

                Map<String, Object> item = new HashMap<>();
                item.put("id", id);
                item.put("title", name);
                item.put("artist", artistName);
                item.put("cover", cover);
                item.put("linkUrl", link);
                result.add(item);
            }
        }
        return result;
    }

    private String extractArtist(JsonNode songNode) {
        // 兼容 artists / ar 两种字段
        JsonNode artistsNode = songNode.has("artists") ? songNode.get("artists") : songNode.get("ar");
        if (artistsNode != null && artistsNode.isArray() && artistsNode.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode artistNode : artistsNode) {
                String name = artistNode.path("name").asText("");
                if (!name.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append(" / ");
                    }
                    sb.append(name);
                }
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
        }
        return "";
    }

    private String extractCover(JsonNode songNode) {
        String cover = songNode.path("album").path("blurPicUrl").asText("");
        if (cover == null || cover.isEmpty()) {
            cover = songNode.path("album").path("picUrl").asText("");
        }
        if (cover == null || cover.isEmpty()) {
            cover = songNode.path("al").path("picUrl").asText("");
        }
        if (cover != null && !cover.isEmpty()) {
            if (cover.startsWith("//")) {
                cover = "https:" + cover;
            } else if (cover.startsWith("http://")) {
                cover = cover.replace("http://", "https://");
            }
            // 统一返回大图
            if (!cover.contains("?param=")) {
                cover = cover + "?param=400y400";
            }
            return cover;
        }
        // 留空以便后续通过详情接口或页面兜底获取真实封面
        return "";
    }

    /**
     * 通过网易云专辑详情接口获取更精确的专辑名 / 艺人 / 封面
     */
    private Map<String, String> fetchAlbumMeta(long albumId) throws IOException, InterruptedException {
        Map<String, String> meta = new HashMap<>();
        String apiUrl = "https://music.163.com/api/album/" + albumId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://music.163.com/")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return meta;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode album = root.path("album");
        if (album.isMissingNode()) {
            return meta;
        }
        String name = album.path("name").asText("");
        if (name != null && !name.isEmpty()) {
            meta.put("title", name);
        }
        String cover = album.path("picUrl").asText("");
        if (cover != null && !cover.isEmpty()) {
            if (cover.startsWith("//")) {
                cover = "https:" + cover;
            } else if (cover.startsWith("http://")) {
                cover = cover.replace("http://", "https://");
            }
            if (!cover.contains("?param=")) {
                cover = cover + "?param=400y400";
            }
            meta.put("cover", cover);
        }
        // 专辑可能有多个艺人，优先从 artists 数组拼接所有艺人名；如果没有再退回单个 artist 字段
        StringBuilder artistNames = new StringBuilder();
        JsonNode artistsNode = album.path("artists");
        if (artistsNode.isArray() && artistsNode.size() > 0) {
            for (JsonNode a : artistsNode) {
                String n = a.path("name").asText("");
                if (n != null && !n.isEmpty()) {
                    if (artistNames.length() > 0) {
                        artistNames.append(" / ");
                    }
                    artistNames.append(n);
                }
            }
        }
        if (artistNames.length() == 0) {
            JsonNode artist = album.path("artist");
            if (!artist.isMissingNode()) {
                String artistName = artist.path("name").asText("");
                if (artistName != null && !artistName.isEmpty()) {
                    artistNames.append(artistName);
                }
            }
        }
        if (artistNames.length() > 0) {
            meta.put("artist", artistNames.toString());
        }

        // “热度数”：优先从 info.likedCount / info.subscribedCount / info.shareCount 中获取
        JsonNode infoNode = album.path("info");
        if (!infoNode.isMissingNode() && infoNode.isObject()) {
            long likedCount = infoNode.path("likedCount").asLong(0);
            long subscribedCount = infoNode.path("subscribedCount").asLong(0);
            long shareCount = infoNode.path("shareCount").asLong(0);
            long fav = likedCount > 0 ? likedCount
                    : (subscribedCount > 0 ? subscribedCount : shareCount);
            if (fav > 0) {
                meta.put("favoriteCount", String.valueOf(fav));
            }
        }
        return meta;
    }

    /**
     * 批量获取歌曲详情，弥补搜索接口封面/艺人缺失或错误
     */
    private Map<Long, Map<String, String>> fetchSongDetails(List<Long> ids) throws IOException, InterruptedException {
        Map<Long, Map<String, String>> map = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return map;
        }
        // Netease API expects JSON array string encoded in query param, needs URL-encode
        // 构造无空格 JSON 数组字符串以避免接口解析异常
        String joined = ids.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        String idsJson = "[" + joined + "]";
        String encodedIds = URLEncoder.encode(idsJson, java.nio.charset.StandardCharsets.UTF_8);
        String apiUrl = "https://music.163.com/api/song/detail/?ids=" + encodedIds;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://music.163.com/")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return map;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode songs = root.path("songs");
        if (!songs.isArray()) {
            return map;
        }
        for (JsonNode song : songs) {
            long id = song.path("id").asLong();
            if (id == 0) continue;
            String cover = song.path("al").path("picUrl").asText("");
            if (cover != null && !cover.isEmpty()) {
                if (cover.startsWith("//")) {
                    cover = "https:" + cover;
                } else if (cover.startsWith("http://")) {
                    cover = cover.replace("http://", "https://");
                }
                if (!cover.contains("?param=")) {
                    cover = cover + "?param=400y400";
                }
            }
            String artist = extractArtist(song);
            Map<String, String> detail = new HashMap<>();
            if (cover != null) {
                detail.put("cover", cover);
            }
            if (artist != null) {
                detail.put("artist", artist);
            }
            map.put(id, detail);
        }
        return map;
    }

    /**
     * 通过网易云评论接口获取单曲的总评论数
     * 示例： https://music.163.com/api/v1/resource/comments/R_SO_4_2734680361?limit=1
     */
    private Long fetchSongCommentTotal(long songId) throws IOException, InterruptedException {
        String apiUrl = "https://music.163.com/api/v1/resource/comments/R_SO_4_" + songId + "?limit=1";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://music.163.com/")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode totalNode = root.path("total");
        if (totalNode.isMissingNode() || !totalNode.isNumber()) {
            return null;
        }
        long total = totalNode.asLong(-1);
        return total >= 0 ? total : null;
    }

    /**
     * 通过网易云艺人页面解析艺人名称
     * 支持示例：https://music.163.com/artist?id=55315624
     */
    private String fetchNeteaseArtistName(String artistUrl) throws IOException, InterruptedException {
        if (artistUrl == null || artistUrl.isBlank()) {
            return "";
        }
        String normalized = artistUrl.trim().replace("http://", "https://");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(normalized))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://music.163.com/")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return "";
        }
        String html = response.body();
        // 优先从 meta og:title 提取："贾真Jahgen - 歌手 - 网易云音乐"
        String name = extractByPattern(html,
                "<meta[^>]*property=[\"']og:title[\"'][^>]*content=[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);
        if (name == null || name.isEmpty()) {
            // 兜底：从 <title> 中提取
            name = extractByPattern(html,
                    "<title>\\s*([^<]+?)\\s*</title>",
                    Pattern.CASE_INSENSITIVE);
        }
        if (name == null || name.isEmpty()) {
            return "";
        }
        // 去掉固定后缀：" - 歌手 - 网易云音乐"
        name = name.replace(" - 歌手 - 网易云音乐", "").trim();
        return name;
    }

    /**
     * 抓取歌曲详情页封面（兜底使用）
     */
    private String fetchSongCoverFromPage(long songId) throws IOException, InterruptedException {
        String url = "https://music.163.com/song?id=" + songId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://music.163.com/")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return "";
        }
        String html = response.body();
        String cover = extractByPattern(html,
                "<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);
        if (cover != null && !cover.isEmpty()) {
            if (cover.startsWith("//")) {
                cover = "https:" + cover;
            } else if (cover.startsWith("http://")) {
                cover = cover.replace("http://", "https://");
            }
            if (!cover.contains("?param=")) {
                cover = cover + "?param=400y400";
            }
            return cover;
        }
        return "";
    }

    /**
     * 爬取B站视频信息
     */
    private Map<String, String> fetchBilibili(String bvid) throws IOException, InterruptedException {
        // 清理BV号，确保格式正确
        if (!bvid.startsWith("BV")) {
            bvid = "BV" + bvid.replaceAll("^[Bb][Vv]", "");
        }
        
        String apiUrl = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://www.bilibili.com")
                .header("Origin", "https://www.bilibili.com")
                .header("Accept", "application/json,text/plain,*/*")
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("B站API请求失败，状态码: " + response.statusCode());
        }

        String body = response.body();
        
        // 简单JSON解析（可以使用Jackson，但这里为了简单直接用正则）
        Map<String, String> result = new HashMap<>();
        
        // 提取code
        Pattern codePattern = Pattern.compile("\"code\"\\s*:\\s*(\\d+)");
        Matcher codeMatcher = codePattern.matcher(body);
        if (codeMatcher.find()) {
            int code = Integer.parseInt(codeMatcher.group(1));
            if (code != 0) {
                Pattern msgPattern = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");
                Matcher msgMatcher = msgPattern.matcher(body);
                String message = msgMatcher.find() ? msgMatcher.group(1) : "未知错误";
                throw new IOException("B站API返回错误: " + message);
            }
        }
        
        // 提取title
        Pattern titlePattern = Pattern.compile("\"title\"\\s*:\\s*\"([^\"]+)\"");
        Matcher titleMatcher = titlePattern.matcher(body);
        String title = titleMatcher.find() ? titleMatcher.group(1) : "无标题";
        
        // 提取pic（封面）
        Pattern picPattern = Pattern.compile("\"pic\"\\s*:\\s*\"([^\"]+)\"");
        Matcher picMatcher = picPattern.matcher(body);
        String cover = picMatcher.find() ? picMatcher.group(1) : "";
        
        // 处理封面URL
        if (cover != null && !cover.isEmpty()) {
            if (cover.startsWith("//")) {
                cover = "https:" + cover;
            } else if (cover.startsWith("http://")) {
                cover = cover.replace("http://", "https://");
            }
            // B站封面优化
            if (!cover.contains("@")) {
                cover = cover + "@672w_378h_1c.webp";
            }
        } else {
            cover = "https://picsum.photos/800/600?random=" + System.currentTimeMillis();
        }
        
        // 提取播放量（data.stat.view）
        // 尝试多种模式匹配，因为JSON格式可能有变化
        String views = "0";
        Pattern viewPattern1 = Pattern.compile("\"stat\"\\s*:\\s*\\{[^}]*\"view\"\\s*:\\s*(\\d+)");
        Matcher viewMatcher1 = viewPattern1.matcher(body);
        if (viewMatcher1.find()) {
            try {
                long viewCount = Long.parseLong(viewMatcher1.group(1));
                views = formatViews(viewCount);
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        } else {
            // 备用模式：直接查找 "view":数字
            Pattern viewPattern2 = Pattern.compile("\"view\"\\s*:\\s*(\\d+)");
            Matcher viewMatcher2 = viewPattern2.matcher(body);
            if (viewMatcher2.find()) {
                try {
                    long viewCount = Long.parseLong(viewMatcher2.group(1));
                    views = formatViews(viewCount);
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }
        
        result.put("title", title);
        result.put("coverImage", cover);
        result.put("sourceUrl", "https://www.bilibili.com/video/" + bvid);
        result.put("views", views);
        
        return result;
    }

    /**
     * 爬取微信公众号文章信息
     */
    private Map<String, String> fetchWeChat(String articleUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(articleUrl))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://mp.weixin.qq.com/")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("微信公众号请求失败，状态码: " + response.statusCode());
        }

        String html = response.body();
        Map<String, String> result = new HashMap<>();
        
        // 提取标题 - 优先从JavaScript变量中提取
        String title = extractByPattern(html, "var\\s+msg_title\\s*=\\s*\"([^\"]+)\"");
        if (title == null || title.isEmpty()) {
            // 备用：从meta标签提取
            title = extractByPattern(html, "<meta[^>]*property=[\"']og:title[\"'][^>]*content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        }
        if (title == null || title.isEmpty()) {
            title = "无标题";
        }
        
        // 提取封面 - 优先从JavaScript变量中提取
        String cover = extractByPattern(html, "var\\s+msg_cdn_url\\s*=\\s*\"([^\"]+)\"");
        if (cover == null || cover.isEmpty()) {
            // 备用：从meta标签提取
            cover = extractByPattern(html, "<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        }
        
        // 处理封面URL
        if (cover != null && !cover.isEmpty()) {
            if (cover.startsWith("//")) {
                cover = "https:" + cover;
            } else if (cover.startsWith("http://")) {
                cover = cover.replace("http://", "https://");
            }
        } else {
            cover = "https://picsum.photos/800/600?random=" + System.currentTimeMillis();
        }
        
        // 提取链接
        String link = extractByPattern(html, "var\\s+msg_link\\s*=\\s*\"([^\"]+)\"");
        if (link == null || link.isEmpty()) {
            link = articleUrl;
        }
        
        // 提取阅读量 - 从HTML中查找"阅读 数字"的模式
        // 支持的格式：阅读 6484、阅读 1.6万、阅读 1.6k、阅读量 6484、阅读: 6484 等
        String views = "0";
        // 优先匹配带单位的格式（如"阅读 1.6万"、"阅读 1.6k"）
        // 匹配模式：阅读[可选空格/冒号][数字（可能包含小数点）][可选空格/HTML标签][单位（万/k/K/w/W）]
        // 使用非贪婪匹配，允许数字和单位之间有HTML标签
        Pattern readPatternWithUnit = Pattern.compile("阅读[\\s:：]*([\\d,.]+)(?:[\\s<>/]*)([万千kKwW])", Pattern.CASE_INSENSITIVE);
        Matcher readMatcherWithUnit = readPatternWithUnit.matcher(html);
        if (readMatcherWithUnit.find()) {
            String numberStr = readMatcherWithUnit.group(1).replace(",", "").trim();
            String unit = readMatcherWithUnit.group(2).trim();
            try {
                double number = Double.parseDouble(numberStr);
                long readCount = parseViewsWithUnit(number, unit);
                views = formatViews(readCount);
            } catch (NumberFormatException e) {
                // 如果解析失败，直接保留原始格式（如"1.6万"）
                views = numberStr + unit;
            }
        } else {
            // 如果没有匹配到带单位的格式，尝试匹配纯数字格式
            Pattern readPattern = Pattern.compile("阅读[\\s:：]*([\\d,]+)", Pattern.CASE_INSENSITIVE);
            Matcher readMatcher = readPattern.matcher(html);
            if (readMatcher.find()) {
                String readCountStr = readMatcher.group(1).replace(",", "");
                try {
                    long readCount = Long.parseLong(readCountStr);
                    views = formatViews(readCount);
                } catch (NumberFormatException e) {
                    // 如果解析失败，保持默认值
                }
            }
        }
        
        result.put("title", title);
        result.put("coverImage", cover);
        result.put("sourceUrl", link);
        result.put("views", views);
        
        return result;
    }

    /**
     * 爬取网易云音乐歌曲/专辑信息
     * 支持示例：
     *  - https://music.163.com/#/song?id=123456
     *  - https://music.163.com/song?id=123456
     *  - https://music.163.com/#/album?id=123456
     *  - https://music.163.com/album?id=123456
     */
    private Map<String, String> fetchNeteaseMusic(String musicUrl) throws IOException, InterruptedException {
        // 统一成 https，并去掉多余空格，同时兼容 hash 路由形式的链接（#/album?id=xxx）
        String normalized = musicUrl.trim().replace("http://", "https:");
        // 兼容网易云常见分享链接： https://music.163.com/#/album?id=282938569
        if (normalized.contains("#/album?")) {
            normalized = normalized.replace("#/album?", "/album?");
        }
        if (normalized.contains("#/song?")) {
            normalized = normalized.replace("#/song?", "/song?");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(normalized))
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://music.163.com/")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("网易云音乐请求失败，状态码: " + response.statusCode());
        }

        String html = response.body();
        Map<String, String> result = new HashMap<>();

        // 尝试从 URL 中解析出 albumId / songId
        Long albumId = null;
        Long songId = null;
        try {
            URI uri = URI.create(normalized);
            String path = uri.getPath();
            String query = uri.getQuery();
            if (path != null && query != null) {
                String[] parts = query.split("&");
                for (String p : parts) {
                    if (p.startsWith("id=")) {
                        long id = Long.parseLong(p.substring(3));
                        if (path.contains("/album")) {
                            albumId = id;
                        } else if (path.contains("/song")) {
                            songId = id;
                        }
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 1. 标题：页面 <title>xxx - 艺人 - 单曲/专辑 - 网易云音乐</title> 或类似
        String title = extractByPattern(html, "<title>\\s*([^<]+?)\\s*</title>", Pattern.CASE_INSENSITIVE);
        if (title != null && !title.isEmpty()) {
            // 去掉固定后缀
            title = title.replace(" - 单曲 - 网易云音乐", "")
                         .replace(" - 专辑 - 网易云音乐", "")
                         .replace(" - 歌单 - 网易云音乐", "")
                         .trim();
            // 对于「歌曲名 / 专辑名 - 艺人」这种格式，只保留前半部分作为标题
            int idx = title.indexOf(" - ");
            if (idx > 0) {
                title = title.substring(0, idx).trim();
            }
        } else {
            // 兜底：meta og:title
            title = extractByPattern(html,
                    "<meta[^>]*property=[\"']og:title[\"'][^>]*content=[\"']([^\"']+)[\"']",
                    Pattern.CASE_INSENSITIVE);
        }
        if (title == null || title.isEmpty()) {
            title = "未知歌曲";
        }

        // 0. 艺人（歌手）：从 meta 或页面结构中提取
        // 优先：meta property="og:music:artist" content="艺人名"
        String artist = extractByPattern(html,
                "<meta[^>]*property=[\"']og:music:artist[\"'][^>]*content=[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);
        if (artist == null || artist.isEmpty()) {
            // 兼容：有的页面会使用 property="music:musician"
            artist = extractByPattern(html,
                    "<meta[^>]*property=[\"']music:musician[\"'][^>]*content=[\"']([^\"']+)[\"']",
                    Pattern.CASE_INSENSITIVE);
        }
        // 兜底：尝试从原始 <title> 中拆出艺人，如 "专辑名 - 艺人 - 专辑 - 网易云音乐"
        String rawTitle = extractByPattern(html, "<title>\\s*([^<]+?)\\s*</title>", Pattern.CASE_INSENSITIVE);
        if ((artist == null || artist.isEmpty()) && rawTitle != null && rawTitle.contains(" - ")) {
            String[] parts = rawTitle.split("\\s-\\s");
            if (parts.length >= 2) {
                // 一般格式：歌曲名/专辑名 - 艺人 - 单曲/专辑 - 网易云音乐
                artist = parts[1].trim();
            }
        }
        // 有些页面可能把 "专辑名 - 艺人" 一整串塞进 artist 里，或者直接填成艺人主页 URL，这里统一清洗
        if (artist != null && !artist.isEmpty()) {
            artist = artist.trim();
            // 如果直接是一个 URL（例如 https://music.163.com/artist?id=55315624），尝试进一步解析出真正的艺人名
            if (artist.startsWith("http://") || artist.startsWith("https://")) {
                try {
                    String parsedArtist = fetchNeteaseArtistName(artist);
                    if (parsedArtist != null && !parsedArtist.isEmpty()) {
                        artist = parsedArtist;
                    } else {
                        artist = "";
                    }
                } catch (Exception e) {
                    // 如果解析失败，则认为无效艺人，交给后续逻辑/专辑接口兜底
                    artist = "";
                }
            } else {
                // 如果 artist 里包含标题（例如 "Motion Music - 贾真Jahgen"），只保留后半段
                if (title != null && !title.isEmpty() && artist.startsWith(title) && artist.contains(" - ")) {
                    String remain = artist.substring(title.length()).replaceFirst("^\\s*-\\s*", "");
                    if (!remain.isEmpty()) {
                        artist = remain;
                    }
                } else if (artist.contains(" - ") && (artist.endsWith(" - 单曲") || artist.endsWith(" - 专辑"))) {
                    // 兼容部分奇怪格式，取中间段作为艺人
                    String[] aParts = artist.split("\\s-\\s");
                    if (aParts.length >= 2) {
                        artist = aParts[0].trim();
                    }
                }
            }
        }

        // 额外兜底：有些专辑/歌曲页面在正文中会有一行「歌手：A / B / C」，用它补全合作艺人
        String displayArtist = extractByPattern(html, "歌手：\\s*([^<\\n]+)");

        // 2. 封面：meta property="og:image" content="xxx"
        String cover = extractByPattern(html,
                "<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);

        if (cover != null && !cover.isEmpty()) {
            if (cover.startsWith("//")) {
                cover = "https:" + cover;
            } else if (cover.startsWith("http://")) {
                cover = cover.replace("http://", "https://");
            }
        } else {
            cover = "https://picsum.photos/800/800?random=" + System.currentTimeMillis();
        }

        // 3. 计数字段：专辑优先用收藏/分享，单曲优先用评论数（优先使用官方评论/专辑接口，其次 HTML 兜底）
        Long favoriteCount = null;

        // 如果是专辑链接且成功解析出 albumId，则用专辑详情接口覆盖 title/artist/cover/收藏数
        if (albumId != null) {
            try {
                Map<String, String> albumMeta = fetchAlbumMeta(albumId);
                if (albumMeta != null) {
                    String albumTitle = albumMeta.get("title");
                    String albumArtist = albumMeta.get("artist");
                    String albumCover = albumMeta.get("cover");
                    String albumFavorite = albumMeta.get("favoriteCount");
                    if (albumTitle != null && !albumTitle.isEmpty()) {
                        title = albumTitle;
                    }
                    if (albumArtist != null && !albumArtist.isEmpty()) {
                        artist = albumArtist;
                    }
                    if (albumCover != null && !albumCover.isEmpty()) {
                        cover = albumCover;
                    }
                    if (albumFavorite != null && !albumFavorite.isEmpty()) {
                        try {
                            favoriteCount = Long.parseLong(albumFavorite);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
                // 如果专辑详情接口失败，则继续使用上面 HTML 解析到的 title/artist/cover
            }
        }

        // 如果是单曲链接且还没有拿到计数，优先通过官方评论接口获取总评论数
        if (favoriteCount == null && songId != null) {
            try {
                Long commentTotal = fetchSongCommentTotal(songId);
                if (commentTotal != null && commentTotal >= 0) {
                    favoriteCount = commentTotal;
                }
            } catch (Exception ignored) {
                // 忽略接口失败，继续走 HTML 兜底解析
            }
        }

        // 如果接口层面和评论接口都没拿到数，再尝试从 HTML 文本中解析：
        // 单曲优先解析评论数，专辑优先解析收藏 / 分享：
        //  - 评论(978) / 评论（978）
        //  - 评论 共978条评论 / 共978条评论
        //  - 978条评论
        //  - 收藏(8179) / 分享(8179)
        //  - 8179人收藏 / 8179人分享
        //  - 1.4万 人收藏 / 收藏(1.4万) / 1.4万 人分享
        if (favoriteCount == null) {
            String favStr = null;
            boolean isSong = normalized.contains("/song");
            // 单曲优先尝试更稳定的 DOM 计数：id="cnt_comment_count">2320</span>
            if (isSong) {
                favStr = extractByPattern(html,
                        "id=\\\"cnt_comment_count\\\"[^>]*>\\s*([0-9.,万]+)\\s*<");
                // 如果没有命中，再使用基于文案的多种兜底写法
                if (favStr == null || favStr.isEmpty()) {
                    // 顶部小气泡：评论(2320) / 评论（2320）
                    favStr = extractByPattern(html, "评论\\s*[\\(\\（]\\s*([0-9.,万]+)\\s*[\\)\\）]");
                }
                // 底部标题：评论 共2320条评论 / 共2320条评论
                if (favStr == null || favStr.isEmpty()) {
                    favStr = extractByPattern(html, "共\\s*([0-9.,万]+)\\s*条评论");
                }
                // 兜底：978条评论
                if (favStr == null || favStr.isEmpty()) {
                    favStr = extractByPattern(html, "([0-9.,万]+)\\s*条评论");
                }
            }
            // 如果上面没拿到，再尝试收藏 / 分享相关的计数
            if (favStr == null || favStr.isEmpty()) {
                // 注意：extractByPattern 只返回第一个分组，这里用非捕获分组 (?:...)，只把数字作为 group(1)
                favStr = extractByPattern(html, "(?:收藏|分享)\\s*\\(\\s*([0-9.,万]+)\\s*\\)");
                if (favStr == null || favStr.isEmpty()) {
                    favStr = extractByPattern(html, "([0-9.,万]+)\\s*人(?:收藏|分享)");
                }
            }
            if (favStr != null && !favStr.isEmpty()) {
                try {
                    String raw = favStr.trim();
                    long parsed;
                    if (raw.contains("万")) {
                        raw = raw.replace("万", "").replace(",", "");
                        double d = Double.parseDouble(raw);
                        parsed = (long) (d * 10000);
                    } else {
                        raw = raw.replace(",", "");
                        parsed = Long.parseLong(raw);
                    }
                    if (parsed >= 0) {
                        favoriteCount = parsed;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 如果还有 displayArtist（例如「马思唯 / A Few Good Kids Records / 艾志恒Asen」），
        // 并且当前 artist 为空或只有单人，而 displayArtist 明显是多人，则优先采用 displayArtist
        if (displayArtist != null && !displayArtist.isEmpty()) {
            String trimmedDisplay = displayArtist.trim();
            if (trimmedDisplay.contains(" / ")
                    && (artist == null || artist.isEmpty() || !artist.contains(" / "))) {
                artist = trimmedDisplay;
            }
        }

        result.put("title", title);
        result.put("coverImage", cover);
        if (artist != null) {
            result.put("artist", artist);
        }
        if (favoriteCount != null) {
            // 以字符串形式返回，方便前端直接透传
            result.put("favoriteCount", String.valueOf(favoriteCount));
        }
        result.put("sourceUrl", normalized);

        return result;
    }

    /**
     * 爬取得物商品详情页信息，提取商品标题和封面
     * 示例链接：https://dewu.com/product-detail.html?sourceName=pc&spuId=18001883&propertyValueId=0&skuId=916312944
     */
    private Map<String, String> fetchDewuProduct(String productUrl) throws IOException, InterruptedException {
        String normalized = productUrl.trim().replace("http://", "https://");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(normalized))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("得物商品页面请求失败，状态码: " + response.statusCode());
        }

        String html = response.body();
        Map<String, String> result = new HashMap<>();

        // 标题：优先 meta og:title，其次 <title>
        String title = extractByPattern(html,
                "<meta[^>]*property=[\"']og:title[\"'][^>]*content=[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);
        if (title == null || title.isEmpty()) {
            title = extractByPattern(html,
                    "<title>\\s*([^<]+?)\\s*</title>",
                    Pattern.CASE_INSENSITIVE);
        }
        if (title != null) {
            title = title.trim();
            // 得物 PC 站很多页面只有通用标题，这种情况视为未成功解析
            if (title.isEmpty() || title.equals("得物App-新一代潮流网购社区")) {
                title = null;
            }
        }

        // 封面：meta og:image
        String cover = extractByPattern(html,
                "<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);
        if (cover == null || cover.isEmpty()) {
            cover = extractByPattern(html,
                    "<meta[^>]*name=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"']",
                    Pattern.CASE_INSENSITIVE);
        }
        if (cover != null && !cover.isEmpty()) {
            if (cover.startsWith("//")) {
                cover = "https:" + cover;
            } else if (cover.startsWith("http://")) {
                cover = cover.replace("http://", "https://");
            }
        }

        if (title != null && !title.isEmpty()) {
            result.put("title", title);
        }
        if (cover != null && !cover.isEmpty()) {
            result.put("coverImage", cover);
        }
        // 链接始终返回，方便前端使用
        result.put("sourceUrl", normalized);

        return result;
    }

    private String extractByPattern(String text, String pattern) {
        return extractByPattern(text, pattern, 0);
    }

    private String extractByPattern(String text, String pattern, int flags) {
        Pattern p = Pattern.compile(pattern, flags);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    /**
     * 格式化浏览量：将数字转换为类似 "3.8k", "6484", "270k" 的格式
     * @param count 浏览量数字
     * @return 格式化后的字符串
     */
    private String formatViews(long count) {
        if (count < 1000) {
            return String.valueOf(count);
        } else if (count < 10000) {
            // 1k - 9.9k
            double k = count / 1000.0;
            if (k == (long) k) {
                return (long) k + "k";
            } else {
                return String.format("%.1fk", k);
            }
        } else if (count < 100000) {
            // 10k - 99.9k
            double k = count / 1000.0;
            if (k == (long) k) {
                return (long) k + "k";
            } else {
                return String.format("%.1fk", k);
            }
        } else if (count < 1000000) {
            // 100k - 999.9k
            double k = count / 1000.0;
            if (k == (long) k) {
                return (long) k + "k";
            } else {
                return String.format("%.1fk", k);
            }
        } else {
            // 1M+
            double m = count / 1000000.0;
            if (m == (long) m) {
                return (long) m + "M";
            } else {
                return String.format("%.1fM", m);
            }
        }
    }

    /**
     * 解析带单位的阅读量数字
     * @param number 数字部分（如 1.6）
     * @param unit 单位（如 "万"、"k"、"K"）
     * @return 转换后的纯数字
     */
    private long parseViewsWithUnit(double number, String unit) {
        if (unit == null || unit.isEmpty()) {
            return (long) number;
        }
        unit = unit.trim();
        // 处理"万"单位（中文）
        if (unit.equals("万") || unit.equalsIgnoreCase("w")) {
            return (long) (number * 10000);
        }
        // 处理"k"单位（千）
        if (unit.equalsIgnoreCase("k")) {
            return (long) (number * 1000);
        }
        // 如果没有匹配的单位，返回原数字
        return (long) number;
    }
}





