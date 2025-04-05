// src/main/java/com/pokerapp/api/controller/SpotifyController.java
package com.pokerapp.api.controller;

import com.pokerapp.service.LyricsService;
import com.pokerapp.service.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    @Autowired
    private SpotifyService spotifyService;

    @Autowired
    private LyricsService lyricsService;

    @GetMapping("/login")
    public RedirectView login() {
        String state = UUID.randomUUID().toString();
        String authUrl = spotifyService.createAuthorizationUrl(state);
        return new RedirectView(authUrl);
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam(required = false) String code,
                                 @RequestParam(required = false) String error,
                                 @RequestParam String state) {
        if (error != null) {
            return new RedirectView("https://127.0.0.1:3000/spotify?error=" + error);
        }

        Map<String, Object> tokens = spotifyService.exchangeCodeForTokens(code);
        String accessToken = (String) tokens.get("access_token");
        String refreshToken = (String) tokens.get("refresh_token");
        Long expiresAt = (Long) tokens.get("expires_at");

        // Redirect to the FRONTEND (not backend) with tokens
        return new RedirectView(
                "https://127.0.0.1:3000/spotify#access_token=" + accessToken +
                        "&refresh_token=" + refreshToken +
                        "&expires_at=" + expiresAt
        );
    }

    @GetMapping("/refresh_token")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestParam String refresh_token) {
        Map<String, Object> tokens = spotifyService.refreshAccessToken(refresh_token);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/lyrics")
    public ResponseEntity<Map<String, Object>> getLyrics(
            @RequestParam String artist,
            @RequestParam String title) {
        Map<String, Object> lyrics = lyricsService.fetchLyrics(artist, title);
        return ResponseEntity.ok(lyrics);
    }
}