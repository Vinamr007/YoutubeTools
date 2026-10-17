package com.YouTubeTools.Controller;

import com.YouTubeTools.Model.SearchVideo;
import com.YouTubeTools.Service.YouTubeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/youtube")
public class YouTubeTagsController {

    private final YouTubeService service;

    public YouTubeTagsController(YouTubeService service) {
        this.service = service;
    }

    @PostMapping("/search")
    public String videoTags(@RequestParam("videoTitle") String videoTitle, Model model) {

        if (videoTitle == null || videoTitle.trim().isEmpty()) {
            model.addAttribute("error", "Video Title is Required");
            return "home";
        }

        try {
      // calling the youTubeService.searchVideos(videoTitle) from service layer & stored in results
            List<SearchVideo> results =
                    service.searchVideos(videoTitle);

            if (!results.isEmpty()) {

                // First video = primary video
                model.addAttribute("primaryVideo", results.get(0));

                // Remaining videos = related videos
                if (results.size() > 1) {
                    model.addAttribute("relatedVideos", results.subList(1, results.size()));
                }

                // Combine all tags
                StringBuilder allTags =
                        new StringBuilder();

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