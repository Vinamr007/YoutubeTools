package com.YouTubeTools.Controller;

import com.YouTubeTools.Model.SearchVideo;
import com.YouTubeTools.Service.YouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/youtube")
public class YouTubeVideoController {

    private final YouTubeService youTubeService;

    public YouTubeVideoController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @PostMapping("/video-details")
    public ResponseEntity<?> getVideoDetails(
            @RequestBody VideoRequest request) {

        try {

            String videoId = request.getVideoId();

            if (videoId == null || videoId.isEmpty()) {

                Map<String, String> error = new HashMap<>();
                error.put("message", "Video ID is required");

                return ResponseEntity.badRequest().body(error);
            }

            List<SearchVideo> videos =
                    youTubeService.getVideoDetails(videoId);

            if (videos.isEmpty()) {

                Map<String, String> error = new HashMap<>();
                error.put("message", "No video found with this ID");

                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(videos.get(0));

        } catch (Exception e) {

            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());

            return ResponseEntity.internalServerError()
                    .body(error);
        }
    }

    public static class VideoRequest {

        private String videoId;

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }
    }
}