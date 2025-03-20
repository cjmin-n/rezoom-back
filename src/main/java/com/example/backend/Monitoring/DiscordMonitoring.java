package com.example.backend.Monitoring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component  // Spring 빈으로 등록
public class DiscordMonitoring {

    @Value("${discord.webhook.url}")  // application.yml에서 값을 읽어옴
    private String discordWebhookUrl;

    public void sendAlert(String errorMessage) {
        if (discordWebhookUrl == null || discordWebhookUrl.isEmpty()) {
            System.out.println("Discord Webhook URL이 설정되지 않았습니다.");
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> request = new HashMap<>();
        request.put("content", "🚨 **에러 발생!** 🚨\n```" + errorMessage + "```");

        try {
            restTemplate.postForObject(discordWebhookUrl, request, String.class);
        } catch (Exception e) {
            System.out.println("❌ Discord Webhook 전송 실패: " + e.getMessage());
        }
    }
}