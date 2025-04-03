package com.example.backend.Monitoring.back;

import com.example.backend.Monitoring.DiscordNotifier;
import com.example.backend.config.aws.EnvUtils;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackErrorController {

    private final DiscordNotifier notifier;
    private static final String SPRING_ERROR_CHANNEL_ID = "1355363528936132639";

    public void sendErrorAlert(String errorMessage) {
        if (SPRING_ERROR_CHANNEL_ID == null || SPRING_ERROR_CHANNEL_ID.isEmpty()) {
            System.out.println("❌ Discord 채널 ID가 설정되지 않았습니다.");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚨 Spring 서버 에러 발생")
                .setDescription("```" + truncate(errorMessage) + "```")
                .setColor(0xE74C3C); // 붉은 색
        notifier.sendEmbedBuilder(SPRING_ERROR_CHANNEL_ID, embed);
    }

    private String truncate(String msg) {
        return msg.length() > 1000 ? msg.substring(0, 1000) + "..." : msg;
    }
}
