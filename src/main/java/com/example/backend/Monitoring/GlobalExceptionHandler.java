//package com.example.backend.Monitoring;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.context.request.WebRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//
//@Slf4j  // ✅ 로그 출력 추가
//@ControllerAdvice
//@RequiredArgsConstructor  // ✅ 생성자 자동 주입 (Spring 공식 권장 방식)
//public class GlobalExceptionHandler {
//
//    private final DiscordMonitoring discordMonitoring;  // ✅ 생성자 주입
//
//    @ExceptionHandler(Exception.class) // 모든 예외 감지
//    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
//        // 에러 발생 위치를 포함한 상세 정보 생성
//        String errorMessage = getErrorDetails(ex);
//
//        // Discord Webhook으로 에러 전송
//        discordMonitoring.sendAlert(errorMessage);
//
//        // 서버 로그에도 에러 출력
//        log.error("❌ 서버 오류 발생: {}", errorMessage);
//
//        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    // 에러 상세 정보 가져오기 (Stack Trace 포함)
//    private String getErrorDetails(Exception ex) {
//        StringWriter sw = new StringWriter();
//        PrintWriter pw = new PrintWriter(sw);
//        ex.printStackTrace(pw);  // 전체 Stack Trace 저장
//        return String.format(
//                "**🚨 오류 발생! 🚨**\n" +
//                        "**메시지:** %s\n" +
//                        "**클래스:** %s\n" +
//                        "**메서드:** %s\n" +
//                        "**라인:** %d\n",
//                ex.getMessage(),
//                ex.getStackTrace()[0].getClassName(),
//                ex.getStackTrace()[0].getMethodName(),
//                ex.getStackTrace()[0].getLineNumber()
//        );
//    }
//}
