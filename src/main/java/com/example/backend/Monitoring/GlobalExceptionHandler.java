package com.example.backend.Monitoring;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final DiscordMonitoring discordMonitoring;

    @Autowired  // DiscordMonitoring 빈을 주입받음
    public GlobalExceptionHandler(DiscordMonitoring discordMonitoring) {
        this.discordMonitoring = discordMonitoring;
    }

    @ExceptionHandler(Exception.class) // 모든 예외 감지
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        // 에러 발생 위치를 포함한 상세 정보 생성
        String errorMessage = getErrorDetails(ex);

        // Discord Webhook으로 에러 전송
        discordMonitoring.sendAlert(errorMessage);

        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 에러 상세 정보 가져오기 (Stack Trace 포함)
    private String getErrorDetails(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);  // 전체 Stack Trace 저장
        return "**🚨 오류 발생! 🚨**\n" +
                "**메시지:** " + ex.getMessage() + "\n" +
                "**클래스:** " + ex.getStackTrace()[0].getClassName() + "\n" +
                "**메서드:** " + ex.getStackTrace()[0].getMethodName() + "\n" +
                "**라인:** " + ex.getStackTrace()[0].getLineNumber() + "\n";
    }
}