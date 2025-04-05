// src/main/java/com/pokerapp/service/SpotifyService.java
package com.pokerapp.service.spadehub;

import core.GLA;
import genius.SongSearch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final GLA geniusLyricsApi = new GLA();
    private final Pattern bracketsPattern = Pattern.compile("\\[.*?\\]");

    public SpotifyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ===== Spotify Authentication Methods =====

    public String createAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromHttpUrl("https://accounts.spotify.com/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("scope", "user-read-private user-read-email user-read-playback-state user-modify-playback-state user-read-currently-playing app-remote-control streaming playlist-read-private playlist-read-collaborative playlist-modify-private playlist-modify-public")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    public Map<String, Object> exchangeCodeForTokens(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://accounts.spotify.com/api/token",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        return processTokenResponse(response.getBody());
    }

    public Map<String, Object> refreshAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://accounts.spotify.com/api/token",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        return processTokenResponse(response.getBody());
    }

    private Map<String, Object> processTokenResponse(Map<String, Object> tokenData) {
        // Calculate expiration timestamp
        Integer expiresIn = (Integer) tokenData.get("expires_in");
        Long expirationTimestamp = Instant.now().getEpochSecond() + expiresIn;

        Map<String, Object> result = new HashMap<>(tokenData);
        result.put("expires_at", expirationTimestamp);

        return result;
    }

    // ===== Genius Lyrics Methods =====

    /**
     * Searches for lyrics by artist and title using the Genius API
     * @param artist Artist name
     * @param title Song title
     * @return Map containing the song details and lyrics
     */
    public Map<String, Object> fetchLyrics(String artist, String title) {
        Map<String, Object> result = new HashMap<>();
        result.put("artist", artist);
        result.put("title", title);

        try {
            // Search for song with the library
            SongSearch search = geniusLyricsApi.search(artist + " " + title);

            if (search.getHits().isEmpty()) {
                result.put("error", "No lyrics found for this song");
                return result;
            }

            // Get the first hit
            SongSearch.Hit hit = search.getHits().getFirst();

            // Get lyrics and clean them
            String lyrics = hit.fetchLyrics();
            if (lyrics == null || lyrics.isEmpty()) {
                result.put("error", "Lyrics could not be retrieved");
                return result;
            }

            // Remove bracket sections like [Chorus] if needed
            String cleanedLyrics = bracketsPattern.matcher(lyrics).replaceAll("");

            // Build result
            result.put("lyrics", cleanedLyrics);
            result.put("title", hit.getTitle());
            result.put("artist", hit.getArtist().getName());
            result.put("imageUrl", hit.getImageUrl());

            return result;

        } catch (IOException e) {
            result.put("error", "Error fetching lyrics: " + e.getMessage());
            return result;
        }
    }

    /**
     * Create a karaoke timeline with timestamp segments
     * @param artist Artist name
     * @param title Song title
     * @param duration Song duration in seconds
     * @param chunks Number of segments to divide lyrics into
     * @return Map containing timeline data and song information
     */
    public Map<String, Object> generateKaraokeTimeline(String artist, String title, int duration, int chunks) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Get lyrics first
            Map<String, Object> lyricsResult = fetchLyrics(artist, title);

            if (lyricsResult.containsKey("error")) {
                return lyricsResult;
            }

            String lyrics = (String) lyricsResult.get("lyrics");
            String[] lines = lyrics.split("\n");

            // Create timeline
            int linesPerChunk = Math.max(1, lines.length / chunks);
            double intervalDuration = (double) duration / chunks;

            List<Map<String, Object>> timeline = new ArrayList<>();

            for (int i = 0; i < chunks; i++) {
                Map<String, Object> segment = new HashMap<>();

                int startIndex = i * linesPerChunk;
                int endIndex = Math.min(startIndex + linesPerChunk, lines.length);

                if (startIndex >= lines.length) {
                    break;
                }

                StringBuilder chunkText = new StringBuilder();
                for (int j = startIndex; j < endIndex; j++) {
                    chunkText.append(lines[j]).append("\n");
                }

                segment.put("time", i * intervalDuration);
                segment.put("lyrics", chunkText.toString().trim());
                timeline.add(segment);
            }

            result.put("timeline", timeline);
            result.put("artist", lyricsResult.get("artist"));
            result.put("title", lyricsResult.get("title"));
            result.put("imageUrl", lyricsResult.get("imageUrl"));
            return result;

        } catch (Exception e) {
            result.put("error", "Error creating karaoke timeline: " + e.getMessage());
            return result;
        }
    }
}