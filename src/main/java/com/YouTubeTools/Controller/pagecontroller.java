package com.YouTubeTools.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class pagecontroller {
   
   @GetMapping({"/","home"})
    public String home(){

       return "home";
   }

   
   @GetMapping("/video-details")
    public String videodetails(){
       return "video-details";
   }
    
}
