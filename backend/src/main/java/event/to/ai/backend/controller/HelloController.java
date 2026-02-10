package event.to.ai.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello Spring Boot!";
    }

    @GetMapping("/user")
    public Map<String, Object> getUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1);
        user.put("name", "Br");
        return user;
    }
}