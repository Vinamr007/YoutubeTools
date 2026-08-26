package com.YouTubeTools.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchVideo {

    private String id;

    private String title;

    private String channelTitle;

    private String description;

    private String thumbnailUrl;

    private List<String> tags;

    private String publishedAt;

    private String duration;

    private Long viewCount;

    private Long likeCount;

    public String getTagsAsString() {

        if (tags == null || tags.isEmpty()) {
            return "";
        }

        return String.join(", ", tags);
    }
}