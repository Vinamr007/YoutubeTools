package com.YouTubeTools.Service;


import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ThumbnailService {


    public String extractVideoId(String url){
        if(url.matches("^[a-zA-Z-0-9_-]{11}$")){
            return url;
        }
        String[] patterns={
                "(?:https?:\\/\\/)?(?:www\\.)?youtube\\.com\\/watch\\?v=([a-zA-Z0-9_-]{11})",
                "(?:https?:\\/\\/)?(?:www\\.)?youtu\\.be\\/([a-zA-Z0-9_-]{11})",
                "(?:https?:\\/\\/)?(?:www\\.)?youtube\\.com\\/embed\\/([a-zA-Z0-9_-]{11})"
        };

        for(String pattern:patterns){
            Matcher matcher= Pattern.compile(pattern).matcher(url);
            if(matcher.find()){
                return matcher.group(1);
            }
        }
        return null;
    }
}