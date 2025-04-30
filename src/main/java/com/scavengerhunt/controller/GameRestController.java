package com.scavengerhunt.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/game")
public class GameRestController {

    @PostMapping("/submit-answer") // update user solved landmarks
    public void submitAnswer() {
       
    }

}

