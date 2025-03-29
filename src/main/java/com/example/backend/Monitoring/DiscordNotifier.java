package com.example.backend.Monitoring;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class DiscordNotifier {

    public void sendToDiscord(String webhookUrl, String messageJson) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(messageJson))
                    .header("Content-Type", "application/json")
                    .build();
            client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
