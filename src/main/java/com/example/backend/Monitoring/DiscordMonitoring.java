package com.example.backend.Monitoring;

import com.example.backend.config.aws.EnvUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class DiscordMonitoring {

    private static final String DISCORD_WEBHOOK_URL = EnvUtils.get("DISCORD_SPRING");

    public static void sendAlert(String errorMessage) {
        if (DISCORD_WEBHOOK_URL == null || DISCORD_WEBHOOK_URL.isEmpty()) {
            System.out.println("Discord Webhook URL이 설정되지 않았습니다.");
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> request = new HashMap<>();
        request.put("content", "🚨 **에러 발생!** 🚨\n```" + errorMessage + "```");

        try {
            restTemplate.postForObject(DISCORD_WEBHOOK_URL, request, String.class);
        } catch (Exception e) {
            System.out.println("Discord Webhook 전송 실패: " + e.getMessage());
        }
    }
}