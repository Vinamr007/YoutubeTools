package com.YouTubeTools.Service;

import com.YouTubeTools.Model.SearchVideo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeService {

    @Value("${YOUTUBE_API_KEY}")
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


    public List<SearchVideo> searchVideos(String query) {
        try {
            // First, search for videos
            String searchResponse = searchVideosFromAPI(query);
            List<String> videoIds = extractVideoIdsFromSearch(searchResponse);
            
            if (videoIds.isEmpty()) {
                return new ArrayList<>();
            }

            // Then get detailed video information including tags
            String videosResponse = getVideoDetailsFromAPI(videoIds);
            return parseVideoDetails(videosResponse);

        } catch (Exception e) {
            throw new RuntimeException("Error searching videos: " + e.getMessage(), e);
        }
    }

    public List<SearchVideo> getVideoDetails(String videoId) {

        try {

            List<String> videoIds = new ArrayList<>();
            videoIds.add(videoId);

            String videosResponse =
                    getVideoDetailsFromAPI(videoIds);

            return parseVideoDetails(videosResponse);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error getting video details: "
                            + e.getMessage(), e);
        }
    }

    private String searchVideosFromAPI(String query) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.googleapis.com")
                        .path("/youtube/v3/search")
                        .queryParam("part", "snippet")
                        .queryParam("q", query)
                        .queryParam("type", "video")
                        .queryParam("maxResults", maxRelatedVideos + 1)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .onStatus(
                        status -> status.value() == 400,
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        "Invalid request to YouTube API"))
                )
                .onStatus(
                        status -> status.value() == 403,
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        "YouTube API key or quota problem"))
                )
                .onStatus(
                        status -> status.value() == 404,
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        "YouTube resource not found"))
                )
                .bodyToMono(String.class)
                .block();
    }

    private String getVideoDetailsFromAPI(List<String> videoIds) {

        String videoIdsString = String.join(",", videoIds);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.googleapis.com")
                        .path("/youtube/v3/videos")
                        .queryParam("part", "snippet,statistics,contentDetails")
                        .queryParam("id", videoIdsString)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .onStatus(
                        status -> status.value() == 400,
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        "Invalid video request"))
                )
                .onStatus(
                        status -> status.value() == 403,
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        "YouTube API key or quota problem"))
                )
                .onStatus(
                        status -> status.value() == 404,
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        "Video not found"))
                )
                .bodyToMono(String.class)
                .block();
    }

    private List<String> extractVideoIdsFromSearch(String searchResponse) throws Exception {
        List<String> videoIds = new ArrayList<>();
        JsonNode root = objectMapper.readTree(searchResponse);
        JsonNode items = root.get("items");

        if (items != null && items.isArray()) {
            for (JsonNode item : items) {
                JsonNode id = item.get("id");
                if (id != null && id.has("videoId")) {
                    videoIds.add(id.get("videoId").asText());
                }
            }
        }

        return videoIds;
    }

    private List<SearchVideo> parseVideoDetails(String videosResponse) throws Exception {
        List<SearchVideo> videos = new ArrayList<>();
        JsonNode root = objectMapper.readTree(videosResponse);
        JsonNode items = root.get("items");

        if (items != null && items.isArray()) {
            for (JsonNode item : items) {
                SearchVideo video = parseVideoFromJson(item);
                videos.add(video);
            }
        }

        return videos;
    }

    private SearchVideo parseVideoFromJson(JsonNode item) {
        JsonNode snippet = item.get("snippet");
        JsonNode statistics = item.get("statistics");
        JsonNode contentDetails = item.get("contentDetails");

        List<String> tags = new ArrayList<>();
        if (snippet.has("tags") && snippet.get("tags").isArray()) {
            for (JsonNode tag : snippet.get("tags")) {
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
                .duration(contentDetails.has("duration") ? contentDetails.get("duration").asText() : "")
                .viewCount(statistics.has("viewCount") ? Long.parseLong(statistics.get("viewCount").asText()) : 0L)
                .likeCount(statistics.has("likeCount") ? Long.parseLong(statistics.get("likeCount").asText()) : 0L)
                .build();
    }
}