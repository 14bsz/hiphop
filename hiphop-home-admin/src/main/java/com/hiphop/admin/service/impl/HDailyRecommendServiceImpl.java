package com.hiphop.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiphop.admin.entity.HDailyRecommend;
import com.hiphop.admin.mapper.HDailyRecommendMapper;
import com.hiphop.admin.service.HDailyRecommendService;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Service
public class HDailyRecommendServiceImpl extends ServiceImpl<HDailyRecommendMapper, HDailyRecommend> implements HDailyRecommendService {

    private static final Logger log = LoggerFactory.getLogger(HDailyRecommendServiceImpl.class);
    
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    @Override
    public int fetchDailyRecommend(String playlistId, int count) {
        try {
            // 1. 获取歌单详情,获取歌单中的所有歌曲ID（支持多个歌单，用逗号分隔）
            List<Long> allSongIds = new ArrayList<>();
            if (playlistId != null && !playlistId.trim().isEmpty()) {
                String[] ids = playlistId.split(",");
                for (String id : ids) {
                    if (id != null && !id.trim().isEmpty()) {
                        List<Long> subIds = fetchPlaylistSongs(id.trim());
                        if (subIds != null && !subIds.isEmpty()) {
                            allSongIds.addAll(subIds);
                        }
                    }
                }
            }
            if (allSongIds.isEmpty()) {
                return 0;
            }
            
            log.info("歌单中总共有{}首歌曲", allSongIds.size());

            // 2. 获取最近3天已推荐过的歌曲标题（缩短到3天，增加新歌池）
            Set<String> recentTitles = getRecentRecommendedTitles(3);
            log.info("最近3天已推荐过{}首歌曲", recentTitles.size());
            
            // 3. 随机选择指定数量的歌曲（尝试获取不重复的歌曲）
            List<Long> selectedIds = randomSelectWithExclusion(allSongIds, count, recentTitles);

            // 4. 获取歌曲详细信息
            List<Map<String, Object>> songs = fetchSongsDetail(selectedIds);

            // 5. 保存到数据库
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = sdf.format(today);

            // 先删除今天已有的推荐
            LambdaQueryWrapper<HDailyRecommend> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(HDailyRecommend::getRecommendDate, todayStr);
            this.remove(queryWrapper);

            // 保存新的推荐
            int sortNo = 0;
            for (Map<String, Object> song : songs) {
                HDailyRecommend recommend = new HDailyRecommend();
                recommend.setTitle((String) song.get("title"));
                recommend.setArtist((String) song.get("artist"));
                recommend.setCover((String) song.get("cover"));
                recommend.setLinkUrl((String) song.get("linkUrl"));
                recommend.setBadge("HOTTTTT");
                recommend.setRecommendDate(today);
                recommend.setSortNo(sortNo++);
                recommend.setStatus(1);
                this.save(recommend);
            }

            return songs.size();
        } catch (Exception e) {
            log.error("抓取每日推荐失败", e);
            return 0;
        }
    }

    @Override
    public List<HDailyRecommend> getRecommendByDate(String date) {
        LambdaQueryWrapper<HDailyRecommend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HDailyRecommend::getRecommendDate, date);
        queryWrapper.eq(HDailyRecommend::getStatus, 1);
        queryWrapper.orderByAsc(HDailyRecommend::getSortNo);
        return this.list(queryWrapper);
    }

    @Override
    public List<HDailyRecommend> getLatestRecommend(int limit) {
        LambdaQueryWrapper<HDailyRecommend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HDailyRecommend::getStatus, 1);
        queryWrapper.orderByDesc(HDailyRecommend::getRecommendDate);
        queryWrapper.orderByAsc(HDailyRecommend::getSortNo);
        queryWrapper.last("LIMIT " + limit);
        return this.list(queryWrapper);
    }

    /**
     * 获取歌单中的所有歌曲ID
     * 使用多个API接口组合，确保获取完整数据
     */
    private List<Long> fetchPlaylistSongs(String playlistId) throws IOException, InterruptedException {
        List<Long> songIds = new ArrayList<>();
        
        // 方案1：尝试使用v3版本的API（可获取完整数据）
        try {
            String apiUrl = "https://music.163.com/api/v3/playlist/detail?id=" + playlistId + "&n=1000";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", "https://music.163.com/")
                    .header("Cookie", "appver=8.7.30") // 模拟客户端
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                
                // v3 API的返回结构
                JsonNode playlist = root.path("playlist");
                JsonNode trackIds = playlist.path("trackIds");
                
                if (trackIds.isArray() && trackIds.size() > 0) {
                    log.info("使用v3 API成功获取{}首歌曲ID", trackIds.size());
                    for (JsonNode trackId : trackIds) {
                        long id = trackId.path("id").asLong();
                        if (id > 0) {
                            songIds.add(id);
                        }
                    }
                    return songIds;
                }
                
                // 如果v3的trackIds为空，尝试从tracks中获取
                JsonNode tracks = playlist.path("tracks");
                if (tracks.isArray() && tracks.size() > 0) {
                    log.info("从tracks获取{}首歌曲", tracks.size());
                    for (JsonNode track : tracks) {
                        long id = track.path("id").asLong();
                        if (id > 0) {
                            songIds.add(id);
                        }
                    }
                    return songIds;
                }
            }
        } catch (Exception e) {
            log.warn("使用v3 API失败，尝试备用API: {}", e.getMessage());
        }
        
        // 方案2：如果v3 API失败，尝试使用原有API
        try {
            String apiUrl = "https://music.163.com/api/playlist/detail?id=" + playlistId;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", "https://music.163.com/")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode tracks = root.path("result").path("tracks");
                
                if (tracks.isArray()) {
                    log.info("使用原有API获取{}首歌曲", tracks.size());
                    for (JsonNode track : tracks) {
                        long id = track.path("id").asLong();
                        if (id > 0) {
                            songIds.add(id);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取歌单失败", e);
        }
        
        if (songIds.isEmpty()) {
            throw new IOException("无法获取歌单数据，请检查歌单ID是否正确");
        }
        
        return songIds;
    }

    /**
     * 随机选择指定数量的元素
     */
    private List<Long> randomSelect(List<Long> source, int count) {
        if (source.size() <= count) {
            return new ArrayList<>(source);
        }
        
        List<Long> copy = new ArrayList<>(source);
        Collections.shuffle(copy);
        return copy.subList(0, count);
    }

    /**
     * 获取最近N天已推荐过的歌曲标题
     */
    private Set<String> getRecentRecommendedTitles(int days) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            String startDate = sdf.format(calendar.getTime());
            
            LambdaQueryWrapper<HDailyRecommend> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(HDailyRecommend::getRecommendDate, startDate);
            queryWrapper.select(HDailyRecommend::getTitle);
            
            List<HDailyRecommend> recentRecommends = this.list(queryWrapper);
            return recentRecommends.stream()
                    .map(HDailyRecommend::getTitle)
                    .filter(title -> title != null && !title.isEmpty())
                    .collect(java.util.stream.Collectors.toSet());
        } catch (Exception e) {
            log.warn("获取最近推荐失败", e);
            return new HashSet<>();
        }
    }

    /**
     * 随机选择歌曲，并尽量避免最近已推荐过的
     */
    private List<Long> randomSelectWithExclusion(List<Long> allSongIds, int count, Set<String> excludeTitles) 
            throws IOException, InterruptedException {
        if (allSongIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 如果没有要排除的歌曲，直接随机选择
        if (excludeTitles == null || excludeTitles.isEmpty()) {
            return randomSelect(allSongIds, count);
        }
        
        // 最多尝试3次，每次抽取的数量逐渐增加
        for (int attempt = 0; attempt < 3; attempt++) {
            // 逐次增加抽样数量：第1次抽20首，第2次抽30首，第3次抽所有
            int sampleSize = attempt == 2 ? allSongIds.size() : count + (attempt + 1) * 10;
            if (sampleSize > allSongIds.size()) {
                sampleSize = allSongIds.size();
            }
            
            log.info("第{}次尝试，从{}首歌中抽取{}首候选", attempt + 1, allSongIds.size(), sampleSize);
            
            // 随机选择一些歌曲
            List<Long> candidates = randomSelect(allSongIds, sampleSize);
            
            // 获取这些歌曲的详情
            List<Map<String, Object>> candidateSongs = fetchSongsDetail(candidates);
            
            // 过滤掉最近已推荐过的
            List<Long> filtered = new ArrayList<>();
            for (int i = 0; i < candidateSongs.size() && filtered.size() < count; i++) {
                Map<String, Object> song = candidateSongs.get(i);
                String title = (String) song.get("title");
                if (!excludeTitles.contains(title)) {
                    filtered.add(candidates.get(i));
                    log.debug("找到新歌：{}", title);
                } else {
                    log.debug("跳过已推荐歌曲：{}", title);
                }
            }
            
            log.info("第{}次尝试，找到{}首新歌，目标{}首", attempt + 1, filtered.size(), count);
            
            // 如果找到足够的歌曲，返回
            if (filtered.size() >= count) {
                log.info("成功找到{}首新歌", filtered.size());
                return filtered.subList(0, count);
            }
        }
        
        // 如果多次尝试后仍然找不到足够的新歌，则直接随机选择
        log.warn("无法找到足够的新歌（歌单太小或所有歌都已推荐过），回退到纯随机选择");
        return randomSelect(allSongIds, count);
    }

    /**
     * 批量获取歌曲详细信息
     */
    private List<Map<String, Object>> fetchSongsDetail(List<Long> songIds) throws IOException, InterruptedException {
        if (songIds == null || songIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 构造歌曲ID数组的JSON字符串
        String idsJson = songIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .map(s -> "[" + s + "]")
                .orElse("[]");
        
        String encodedIds = java.net.URLEncoder.encode(idsJson, java.nio.charset.StandardCharsets.UTF_8);
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
            throw new IOException("获取歌曲详情失败，状态码: " + response.statusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode songs = root.path("songs");

        List<Map<String, Object>> result = new ArrayList<>();
        if (songs.isArray()) {
            for (JsonNode song : songs) {
                long id = song.path("id").asLong();
                if (id == 0) continue;

                String title = song.path("name").asText("");
                String artist = extractArtist(song);
                String cover = extractCover(song);
                String linkUrl = "https://music.163.com/song?id=" + id;

                Map<String, Object> item = new HashMap<>();
                item.put("id", id);
                item.put("title", title);
                item.put("artist", artist);
                item.put("cover", cover);
                item.put("linkUrl", linkUrl);
                result.add(item);
            }
        }

        return result;
    }

    /**
     * 提取艺人名称
     */
    private String extractArtist(JsonNode songNode) {
        JsonNode artistsNode = songNode.path("ar");
        if (!artistsNode.isArray() || artistsNode.size() == 0) {
            artistsNode = songNode.path("artists");
        }
        
        if (artistsNode.isArray() && artistsNode.size() > 0) {
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
        return "未知艺人";
    }

    /**
     * 提取封面图片
     */
    private String extractCover(JsonNode songNode) {
        String cover = songNode.path("al").path("picUrl").asText("");
        if (cover.isEmpty()) {
            cover = songNode.path("album").path("picUrl").asText("");
        }
        
        if (!cover.isEmpty()) {
            if (cover.startsWith("//")) {
                cover = "https:" + cover;
            } else if (cover.startsWith("http://")) {
                cover = cover.replace("http://", "https://");
            }
            // 添加参数获取合适尺寸的图片
            if (!cover.contains("?param=")) {
                cover = cover + "?param=400y400";
            }
            return cover;
        }
        
        return "https://picsum.photos/400/400?random=" + System.currentTimeMillis();
    }
}
