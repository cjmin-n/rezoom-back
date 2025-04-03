package com.example.backend.Monitoring;

import com.example.backend.Monitoring.DiscordNotifier;
import com.example.backend.config.aws.EnvUtils;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordNotifier notifier;
    private static final String SPRING_ERROR_CHANNEL_ID = "1355363528936132639";

    @ExceptionHandler(Exception.class) // 모든 예외 감지
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        String message = ex.getMessage();
        String className = ex.getStackTrace()[0].getClassName();
        String methodName = ex.getStackTrace()[0].getMethodName();
        int lineNumber = ex.getStackTrace()[0].getLineNumber();

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔥 Spring 서버 예외 발생")
                .addField("메시지", message != null ? message : "(no message)", false)
                .addField("위치", className + "." + methodName + "(): " + lineNumber + "라인", false)
                .addField("요약 스택트레이스", shorten(stackTrace), false)
                .setColor(0xE74C3C); // 빨간색

        notifier.sendEmbedBuilder(SPRING_ERROR_CHANNEL_ID, embed);

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String shorten(String trace) {
        if (trace.length() > 1000) {
            return "```" + trace.substring(0, 1000) + "...```";
        }
        return "```" + trace + "```";
    }
}
