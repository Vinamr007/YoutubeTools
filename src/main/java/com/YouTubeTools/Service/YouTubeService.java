package com.YouTubeTools.Service;


import com.YouTubeTools.Model.SearchVideo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.search.url}")
    private String searchUrl;

    @Value("${youtube.api.videos.url}")
    private String videosUrl;

    @Value("${youtube.api.max.related.videos:3}")
    private int maxRelatedVideos;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public YouTubeService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    // =====================================================
    // SEARCH VIDEOS
    // =====================================================

    public List<SearchVideo> searchVideos(String query) {

        try {

            String searchResponse = searchVideosFromAPI(query);

            List<String> videoIds =
                    extractVideoIdsFromSearch(searchResponse);

            if (videoIds.isEmpty()) {
                return new ArrayList<>();
            }

            String videosResponse =
                    getVideoDetailsFromAPI(videoIds);

            return parseVideoDetails(videosResponse);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error searching YouTube videos: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =====================================================
    // GET ONE VIDEO DETAILS
    // =====================================================

    public List<SearchVideo> getVideoDetails(String videoId) {

        try {

            List<String> videoIds =
                    new ArrayList<>();

            videoIds.add(videoId);

            String videosResponse =
                    getVideoDetailsFromAPI(videoIds);

            return parseVideoDetails(videosResponse);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error getting video details: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =====================================================
    // SEARCH API
    // =====================================================

    private String searchVideosFromAPI(String query) {

        var uri = UriComponentsBuilder
                .fromUriString(searchUrl)
                .queryParam("part", "snippet")
                .queryParam("q", query)
                .queryParam("type", "video")
                .queryParam(
                        "maxResults",
                        maxRelatedVideos + 1
                )
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // =====================================================
    // VIDEO DETAILS API
    // =====================================================

    private String getVideoDetailsFromAPI(
            List<String> videoIds) {

        String videoIdsString =
                String.join(",", videoIds);

        var uri = UriComponentsBuilder
                .fromUriString(videosUrl)
                .queryParam(
                        "part",
                        "snippet,statistics,contentDetails"
                )
                .queryParam("id", videoIdsString)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // =====================================================
    // EXTRACT VIDEO IDs
    // =====================================================

    private List<String> extractVideoIdsFromSearch(
            String searchResponse)
            throws Exception {

        List<String> videoIds =
                new ArrayList<>();

        JsonNode root =
                objectMapper.readTree(searchResponse);

        JsonNode items =
                root.get("items");

        if (items != null && items.isArray()) {

            for (JsonNode item : items) {

                JsonNode id =
                        item.get("id");

                if (id != null &&
                        id.has("videoId")) {

                    videoIds.add(
                            id.get("videoId").asText()
                    );
                }
            }
        }

        return videoIds;
    }

    // =====================================================
    // PARSE VIDEO DETAILS
    // =====================================================

    private List<SearchVideo> parseVideoDetails(
            String videosResponse)
            throws Exception {

        List<SearchVideo> videos =
                new ArrayList<>();

        JsonNode root =
                objectMapper.readTree(
                        videosResponse
                );

        JsonNode items =
                root.get("items");

        if (items != null && items.isArray()) {

            for (JsonNode item : items) {

                SearchVideo video =
                        parseVideoFromJson(item);

                videos.add(video);
            }
        }

        return videos;
    }

    // =====================================================
    // CONVERT JSON TO SearchVideo
    // =====================================================

    private SearchVideo parseVideoFromJson(
            JsonNode item) {

        JsonNode snippet = item.get("snippet");

        JsonNode statistics = item.get("statistics");

        JsonNode contentDetails = item.get("contentDetails");

        List<String> tags =
                new ArrayList<>();

        if (snippet != null &&
                snippet.has("tags") &&
                snippet.get("tags").isArray()) {

            for (JsonNode tag :
                    snippet.get("tags")) {

                tags.add(tag.asText());
            }
        }

        return SearchVideo.builder()

                .id(item.get("id").asText())

                .title(snippet.get("title").asText())

                .channelTitle(snippet.get("channelTitle").asText())

                .description(snippet.has("description") ? snippet.get("description").asText() : "")

                .thumbnailUrl(snippet.get("thumbnails").get("high").get("url").asText())

                .tags(tags)

                .publishedAt(snippet.get("publishedAt").asText())

                .duration(contentDetails != null && contentDetails.has("duration") ? contentDetails.get("duration").asText() : "")

                .viewCount(statistics != null && statistics.has("viewCount") ? Long.parseLong(statistics.get("viewCount").asText()) : 0L)

                .likeCount(statistics != null && statistics.has("likeCount") ? Long.parseLong(statistics.get("likeCount").asText()) : 0L)

                .build();
    }
}