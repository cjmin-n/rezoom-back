package com.example.backend.user;
import com.example.backend.entity.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 사용자 추가 API (POST 요청)
    @PostMapping("/add")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.saveUser(user));
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