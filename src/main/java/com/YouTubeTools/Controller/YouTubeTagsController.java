package com.YouTubeTools.Controller;

import com.YouTubeTools.Model.SearchVideo;
import com.YouTubeTools.Service.YouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/youtube")
public class YouTubeTagsController {

    @Autowired
    private YouTubeService youTubeService;

    @Value("${youtube.api.key}")
    private String apiKey;

    private boolean isApiKeyConfiguried() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @PostMapping("/search")
    public String videoTags(@RequestParam("videoTitle") String videoTitle, Model model) {

        if (!isApiKeyConfiguried()) {
            model.addAttribute("error", "API key is not configured");
            return "home";
        }

        if (videoTitle == null || videoTitle.isEmpty()) {
            model.addAttribute("error", "Video Title is Required");
            return "home";
        }

        try {
            List<SearchVideo> results = youTubeService.searchVideos(videoTitle);

            if (!results.isEmpty()) {
                model.addAttribute("primaryVideo", results.get(0)); // first = primary
                if (results.size() > 1) {
                    model.addAttribute("relatedVideos", results.subList(1, results.size())); // others = related
                }
                
                // Add all tags as a single string for copying
                StringBuilder allTags = new StringBuilder();
                for (SearchVideo video : results) {
                    if (video.getTags() != null && !video.getTags().isEmpty()) {
                        if (allTags.length() > 0) {
                            allTags.append(", ");
                        }
                        allTags.append(String.join(", ", video.getTags()));
                    }
                }
                model.addAttribute("allTagsAsString", allTags.toString());
            } else {
                model.addAttribute("error", "No videos found.");
            }

            return "home";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "home";
        }
    }
}
