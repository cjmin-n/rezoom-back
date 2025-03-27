package com.example.backend.user;
import com.example.backend.dto.SignUpRequestDTO;
import com.example.backend.dto.UrlResponseDTO;
import com.example.backend.entity.User;
import com.example.backend.swagger.UserControllerDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
public class UserController implements UserControllerDocs {

    private final UserService userService;

    // 사용자 회원가입 API (POST 요청)
    @PostMapping("/signup")
    public ResponseEntity<UrlResponseDTO> signup(@RequestBody SignUpRequestDTO signUpRequestDTO) {

        userService.saveUser(signUpRequestDTO); // 회원가입 진행 (DB 저장)

        System.out.println("signUpRequestDTO: " + signUpRequestDTO);

        return ResponseEntity.ok(
                UrlResponseDTO.builder()
                        .url("/auth/login") // 회원 가입이 완료된 후 로그인 페이지로 이동
                        .message("회원가입을 성공했습니다.")
                        .build()
        );
    }

    // 모든 사용자 조회 API (GET 요청)
    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 특정 이름으로 사용자 조회 API (GET 요청)
    @GetMapping("/{name}")
    public ResponseEntity<List<User>> getUserByName(@PathVariable String name) {
        return ResponseEntity.ok(userService.getUsersByName(name));
    }
    @GetMapping("/test-error")
    public String testError() {
        throw new RuntimeException("비사아아아아앙!!");
    }
}