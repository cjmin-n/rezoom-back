//package com.example.backend.Monitoring;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Component  // ✅ Spring Bean으로 관리
//public class DiscordMonitoring {
//
//    private final String discordWebhookUrl;
//    private final RestTemplate restTemplate;
//
//    public DiscordMonitoring(
//            @Value("${discord.webhook.url}") String discordWebhookUrl,
//            RestTemplate restTemplate
//    ) {
//        this.discordWebhookUrl = discordWebhookUrl;
//        this.restTemplate = restTemplate;
//    }
//
//    public void sendAlert(String errorMessage) {
//        if (discordWebhookUrl == null || discordWebhookUrl.isEmpty()) {
//            log.warn("⚠️ Discord Webhook URL이 설정되지 않았습니다.");
//            return;
//        }
//
//        Map<String, String> request = new HashMap<>();
//        request.put("content", "🚨 **에러 발생!** 🚨\n```" + errorMessage + "```");
//
//        try {
//            restTemplate.postForObject(discordWebhookUrl, request, String.class);
//            log.info("✅ Discord Webhook 전송 성공");
//        } catch (Exception e) {
//            log.error("❌ Discord Webhook 전송 실패: {}", e.getMessage());
//        }
//    }
//}