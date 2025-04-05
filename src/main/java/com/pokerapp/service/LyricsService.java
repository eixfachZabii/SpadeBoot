// src/main/java/com/pokerapp/service/LyricsService.java
package com.pokerapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LyricsService {

    @Value("${genius.access-token}")
    private String geniusAccessToken;

    private final RestTemplate restTemplate;
    private final Pattern unwantedLinePattern = Pattern.compile(".*\\b(contributors|translations|lyrics)\\b.*", Pattern.CASE_INSENSITIVE);
    private final Pattern bracketsPattern = Pattern.compile("\\[.*?\\]");
    private final Pattern numberEmbedPattern = Pattern.compile("\\d+embed", Pattern.CASE_INSENSITIVE);

    public LyricsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> fetchLyrics(String artist, String title) {
        Map<String, Object> result = new HashMap<>();
        result.put("artist", artist);
        result.put("title", title);

        try {
            // Search for song on Genius
            String searchUrl = "https://api.genius.com/search?q=" + artist + " " + title;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + geniusAccessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> searchResponse = restTemplate.exchange(
                    searchUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            // Extract song ID from search results
            Map<String, Object> searchData = searchResponse.getBody();
            List<Map<String, Object>> hits = (List<Map<String, Object>>)
                    ((Map<String, Object>) searchData.get("response")).get("hits");

            if (hits == null || hits.isEmpty()) {
                result.put("error", "Lyrics not found");
                return result;
            }

            Map<String, Object> song = (Map<String, Object>) hits.get(0).get("result");
            Integer songId = (Integer) song.get("id");

            // Get song details including lyrics path
            String songUrl = "https://api.genius.com/songs/" + songId;
            ResponseEntity<Map> songResponse = restTemplate.exchange(
                    songUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> songData = songResponse.getBody();
            String path = (String) ((Map<String, Object>)
                    ((Map<String, Object>) songData.get("response")).get("song")).get("path");

            // Make a request to the web page to scrape lyrics
            String lyricsUrl = "https://genius.com" + path;
            ResponseEntity<String> lyricsResponse = restTemplate.exchange(
                    lyricsUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            String htmlContent = lyricsResponse.getBody();
            String extractedLyrics = extractLyricsFromHtml(htmlContent);

            // Process the lyrics
            String cleanedLyrics = cleanLyrics(extractedLyrics);

            if (cleanedLyrics.isEmpty()) {
                result.put("error", "Lyrics not found after filtering");
                return result;
            }

            result.put("lyrics", cleanedLyrics);
            return result;

        } catch (Exception e) {
            result.put("error", "An error occurred: " + e.getMessage());
            return result;
        }
    }

    private String extractLyricsFromHtml(String html) {
        // This is a simplified extraction logic
        // In a real implementation, you'd want to use a proper HTML parser like Jsoup
        int lyricsStart = html.indexOf("<div class=\"lyrics\">");
        if (lyricsStart == -1) {
            lyricsStart = html.indexOf("<div data-lyrics-container");
        }

        if (lyricsStart == -1) {
            return "";
        }

        int lyricsEnd = html.indexOf("</div>", lyricsStart);
        if (lyricsEnd == -1) {
            return "";
        }

        String rawLyrics = html.substring(lyricsStart, lyricsEnd);
        // Remove HTML tags
        return rawLyrics.replaceAll("<[^>]*>", "");
    }

    private String cleanLyrics(String lyrics) {
        // Split the lyrics into lines
        String[] lines = lyrics.split("\n");
        List<String> filteredLines = new ArrayList<>();

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            // Check for number embed pattern
            if (numberEmbedPattern.matcher(trimmedLine).find()) {
                break;
            }

            // Filter out unwanted content
            Matcher unwantedMatcher = unwantedLinePattern.matcher(trimmedLine);
            Matcher bracketsMatcher = bracketsPattern.matcher(trimmedLine);

            if (!unwantedMatcher.matches() && !bracketsMatcher.matches()) {
                filteredLines.add(trimmedLine);
            }
        }

        return String.join("\n", filteredLines);
    }
}