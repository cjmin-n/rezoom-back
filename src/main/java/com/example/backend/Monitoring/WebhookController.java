package com.example.backend.Monitoring;

import com.example.backend.config.aws.EnvUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebhookController {


    private final DiscordNotifier notifier;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String ISSUE_WEBHOOK = EnvUtils.get("DISCORD_ISSUE_WEBHOOK");
    private static final String PR_WEBHOOK = EnvUtils.get("DISCORD_PR_WEBHOOK");
    private static final String PUSH_WEBHOOK = EnvUtils.get("DISCORD_PUSH_WEBHOOK");

    public WebhookController(DiscordNotifier notifier) {
        this.notifier = notifier;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String payloadJson) {

        try {
            switch (event) {
                case "issues" -> handleIssue(payloadJson);
                case "pull_request" -> handlePullRequest(payloadJson);
                case "push" -> handlePush(payloadJson);
                default -> System.out.println("Unhandled event: " + event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("Received");
    }

    private void handleIssue(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        String title = node.get("issue").get("title").asText();
        String body = node.get("issue").get("body").asText("");
        String url = node.get("issue").get("html_url").asText();
        String user = node.get("issue").get("user").get("login").asText();

        String message = """
        {
          "embeds": [{
            "title": "📝 New Issue: %s",
            "description": "%s",
            "url": "%s",
            "footer": {"text": "by %s"},
            "color": 3447003
          }]
        }
        """.formatted(title, body, url, user);

        notifier.sendToDiscord(ISSUE_WEBHOOK, message);
    }

    private void handlePullRequest(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        String title = node.get("pull_request").get("title").asText();
        String url = node.get("pull_request").get("html_url").asText();
        String user = node.get("pull_request").get("user").get("login").asText();

        String message = """
        {
          "embeds": [{
            "title": "📦 Pull Request: %s",
            "description": "새로운 PR이 등록되었습니다.",
            "url": "%s",
            "footer": {"text": "by %s"},
            "color": 10181046
          }]
        }
        """.formatted(title, url, user);

        notifier.sendToDiscord(PR_WEBHOOK, message);
    }

    private void handlePush(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        String pusher = node.get("pusher").get("name").asText();
        String branch = node.get("ref").asText().replace("refs/heads/", "");
        String repo = node.get("repository").get("full_name").asText();

        String message = """
        {
          "embeds": [{
            "title": "🚀 New Push to %s",
            "description": "브랜치: %s\\n푸셔: %s",
            "color": 3066993
          }]
        }
        """.formatted(repo, branch, pusher);

        notifier.sendToDiscord(PUSH_WEBHOOK, message);
    }
}

