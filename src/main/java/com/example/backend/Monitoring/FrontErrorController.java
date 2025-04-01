package com.example.backend.Monitoring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class FrontErrorController {

    private static final Dotenv dotenv = Dotenv.load();
    private final DiscordNotifier notifier;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String FRONT_ERROR_WEBHOOK = dotenv.get("DISCORD_FRONT_ERROR_WEBHOOK");

    public FrontErrorController(DiscordNotifier notifier) {
        this.notifier = notifier;
    }

    @PostMapping("/front-error")
    public ResponseEntity<String> receiveFrontError(@RequestBody String payloadJson) {
        try {
            JsonNode node = mapper.readTree(payloadJson);

            String message = node.has("message") ? node.get("message").asText() : "(no message)";
            String stack = node.has("stack") ? node.get("stack").asText() : "(no stack)";
            String url = node.has("url") ? node.get("url").asText() : "(unknown)";
            String userAgent = node.has("userAgent") ? node.get("userAgent").asText() : "(unknown)";
            String time = node.has("time") ? node.get("time").asText() : "(unknown)";

            String discordMessage = """
            {
              "embeds": [{
                "title": "🚨 React 앱 에러 발생",
                "description": "**메시지:** %s\\n**페이지:** %s\\n**시간:** %s",
                "fields": [
                  { "name": "Stack", "value": "%s" },
                  { "name": "브라우저", "value": "%s" }
                ],
                "color": 15158332
              }]
            }
            """.formatted(escape(message), escape(url), escape(time), escapeShort(stack), escape(userAgent));

            notifier.sendToDiscord(FRONT_ERROR_WEBHOOK, discordMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error while processing front error.");
        }

        return ResponseEntity.ok("React error received.");
    }

    // 문자열 escaping (Discord JSON 대응)
    private String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String escapeShort(String text) {
        return escape(text.length() > 200 ? text.substring(0, 200) + "..." : text);
    }
}
